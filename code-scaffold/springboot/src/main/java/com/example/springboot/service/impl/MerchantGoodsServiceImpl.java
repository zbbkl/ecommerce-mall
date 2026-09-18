package com.example.springboot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.Merchant;
import com.example.springboot.entity.Type;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.mapper.MerchantMapper;
import com.example.springboot.mapper.TypeMapper;
import com.example.springboot.service.IMerchantGoodsService;
import com.example.springboot.utils.TokenUtils;
import com.example.springboot.service.impl.MerchantServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 商户侧商品业务实现。
 *
 * 安全约定：merchantId 一律取自登录态（TokenUtils.getCurrentMerchant()），
 * 绝不从请求参数读取；跨商户访问一律 403。
 */
@Service
public class MerchantGoodsServiceImpl implements IMerchantGoodsService {

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private TypeMapper typeMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public IPage<Goods> selectPage(Integer pageNum, Integer pageSize, String name, String state) {
        Merchant current = requireCurrentMerchant();
        Page<Goods> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Goods::getMerchantId, current.getId());
        queryWrapper.like(StrUtil.isNotBlank(name), Goods::getName, name);
        queryWrapper.eq(StrUtil.isNotBlank(state), Goods::getState, state);
        queryWrapper.orderByDesc(Goods::getId);
        Page<Goods> goodsPage = goodsMapper.selectPage(page, queryWrapper);
        goodsPage.getRecords().forEach(this::fillTypeName);
        return goodsPage;
    }

    @Override
    public void save(Goods goods) {
        Merchant current = requireCurrentMerchant();
        requireApproved(current);
        if (StrUtil.isBlank(goods.getName()) || goods.getPrice() == null || goods.getPrice() <= 0) {
            throw new ServiceException("商品名称和价格不合法");
        }
        goods.setId(null);
        goods.setMerchantId(current.getId());
        goods.setAdminId(null);
        if (StrUtil.isBlank(goods.getState())) {
            goods.setState("上架");
        }
        goodsMapper.insert(goods);
    }

    @Override
    public void update(Goods goods) {
        Goods dbGoods = requireOwned(goods.getId());
        Merchant current = requireCurrentMerchant();
        requireApproved(current);
        // 归属字段不允许通过编辑修改
        goods.setMerchantId(dbGoods.getMerchantId());
        goods.setAdminId(dbGoods.getAdminId());
        goodsMapper.updateById(goods);
    }

    @Override
    public void remove(Integer id) {
        requireOwned(id);
        goodsMapper.deleteById(id);
    }

    @Override
    public void updateState(Integer id, String state) {
        requireOwned(id);
        if (!"上架".equals(state) && !"下架".equals(state)) {
            throw new ServiceException("非法的商品状态");
        }
        Goods update = new Goods();
        update.setId(id);
        update.setState(state);
        goodsMapper.updateById(update);
    }

    // ==================== 内部方法 ====================

    /** 取当前登录商户，未登录/角色不符直接 401 */
    private Merchant requireCurrentMerchant() {
        Merchant current = TokenUtils.getCurrentMerchant();
        if (current == null) {
            throw new ServiceException("401", "请先登录商户账号");
        }
        return current;
    }

    /** 入驻状态必须是已通过才允许经营商品 */
    private void requireApproved(Merchant current) {
        if (!MerchantServiceImpl.STATE_APPROVED.equals(current.getState())) {
            throw new ServiceException("店铺尚未通过平台审核，暂不能操作商品");
        }
    }

    /** 校验商品归属当前商户，防越权 */
    private Goods requireOwned(Integer goodsId) {
        Merchant current = requireCurrentMerchant();
        if (goodsId == null) {
            throw new ServiceException("参数不合法");
        }
        Goods dbGoods = goodsMapper.selectById(goodsId);
        if (dbGoods == null) {
            throw new ServiceException("商品不存在");
        }
        if (!current.getId().equals(dbGoods.getMerchantId())) {
            throw new ServiceException("403", "无权操作其他商户的商品");
        }
        return dbGoods;
    }

    private void fillTypeName(Goods goods) {
        Type type = typeMapper.selectById(goods.getTypeId());
        goods.setTypeName(Objects.nonNull(type) ? type.getName() : "未知类型");
    }
}
