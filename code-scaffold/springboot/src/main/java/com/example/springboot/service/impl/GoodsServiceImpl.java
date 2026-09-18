package com.example.springboot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.Collect;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.Merchant;
import com.example.springboot.mapper.*;
import com.example.springboot.entity.Type;
import com.example.springboot.entity.User;
import com.example.springboot.service.IGoodsService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements IGoodsService {

    /** 平台自营商户 ID（迁移脚本/code_scaffold.sql 中的种子商户），管理员代录商品归属到这里 */
    public static final int PLATFORM_MERCHANT_ID = 1;

    @Autowired
    private AdminMapper adminMapper;   // 查询录入管理员姓名

    @Autowired
    private MerchantMapper merchantMapper;   // 查询归属商户名称

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private TypeMapper typeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CollectMapper collectMapper;

    @Override
    public void save(Goods goods) {
        User current = TokenUtils.getCurrentUser();
        goods.setAdminId(current.getId());
        // 管理员代录商品统一归属自营商户；商户自己的商品走 /merchant/goods 专属接口
        goods.setMerchantId(PLATFORM_MERCHANT_ID);
        goodsMapper.insert(goods);
    }

    @Override
    public void update(Goods goods) {
        goodsMapper.updateById(goods);
    }

    @Override
    public void remove(Integer id) {
        goodsMapper.deleteById(id);
    }

    @Override
    public List<Goods> selectAll() {
        return goodsMapper.selectList(null);
    }

    @Override
    public Goods selectById(Integer id) {
        Goods goods = goodsMapper.selectById(id);
        // 下架商品仅管理端可见详情
        if (Objects.nonNull(goods) && !"ADMIN".equals(TokenUtils.getCurrentRole())
                && !"上架".equals(goods.getState())) {
            return null;
        }
        User user = TokenUtils.getCurrentUser();
        if (Objects.nonNull(user)){
            Integer userId = TokenUtils.getCurrentUser().getId();
            LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Collect::getUserId, user.getId());
            queryWrapper.eq(Collect::getGoodsId,id);
            Collect one = collectMapper.selectOne(queryWrapper);
            if (Objects.isNull(one)){
                goods.setIsCollect(false);
            } else {
                goods.setIsCollect(true);
            }
        }
        // 填充归属商户名（详情页展示"进入店铺"入口）
        if (Objects.nonNull(goods) && Objects.nonNull(goods.getMerchantId())){
            Merchant merchant = merchantMapper.selectById(goods.getMerchantId());
            goods.setMerchantName(Objects.nonNull(merchant) ? merchant.getShopName() : null);
        }
        return goods;
    }


    @Override
    public IPage<Goods> selectPage(Integer pageNum, Integer pageSize, String name) {
        Page<Goods> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Goods::getName, name);
        // 非管理员只能看到上架商品（下架商品仅管理端可见）
        if (!"ADMIN".equals(TokenUtils.getCurrentRole())) {
            queryWrapper.eq(Goods::getState, "上架");
        }

        Page<Goods> goodsPage = goodsMapper.selectPage(page, queryWrapper);
        goodsPage.getRecords().stream().forEach(goods -> {
            Type type = typeMapper.selectById(goods.getTypeId());
            goods.setTypeName(Objects.nonNull(type) ? type.getName() : "未知类型");

            Admin admin = adminMapper.selectById(goods.getAdminId());
            goods.setAdminName(Objects.nonNull(admin) ? admin.getName() : "未知用户");

            Merchant merchant = merchantMapper.selectById(goods.getMerchantId());
            goods.setMerchantName(Objects.nonNull(merchant) ? merchant.getShopName() : null);
        });
        return goodsPage;
    }

    @Override
    public List<Goods> times() {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<Goods>();
        queryWrapper.eq(Goods::getState, "上架");   // 首页新品只展示上架商品
        queryWrapper.orderByDesc(Goods::getDate);
        return goodsMapper.selectList(queryWrapper).stream().limit(4).collect(Collectors.toList());
    }

    @Override
    public List<Goods> sales() {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<Goods>();
        queryWrapper.eq(Goods::getState, "上架");   // 首页热销只展示上架商品
        queryWrapper.orderByDesc(Goods::getSales);
        return goodsMapper.selectList(queryWrapper).stream().limit(4).collect(Collectors.toList());
    }
    @Override
    public IPage<Goods> selectPageType(Integer pageNum, Integer pageSize, String name, Integer typeId) {
        Page<Goods> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(name), Goods::getName, name);
        queryWrapper.eq(typeId != 0, Goods::getTypeId, typeId);
        // 非管理员只能看到上架商品
        if (!"ADMIN".equals(TokenUtils.getCurrentRole())) {
            queryWrapper.eq(Goods::getState, "上架");
        }

        Page<Goods> goodsPage = goodsMapper.selectPage(page, queryWrapper);
        goodsPage.getRecords().stream().forEach(goods -> {
            Type type = typeMapper.selectById(goods.getTypeId());
            goods.setTypeName(Objects.nonNull(type) ? type.getName() : "未知类型");

            Admin admin = adminMapper.selectById(goods.getAdminId());
            goods.setAdminName(Objects.nonNull(admin) ? admin.getName() : "未知用户");

            Merchant merchant = merchantMapper.selectById(goods.getMerchantId());
            goods.setMerchantName(Objects.nonNull(merchant) ? merchant.getShopName() : null);
        });
        return goodsPage;
    }

}
