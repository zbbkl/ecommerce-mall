package com.example.springboot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.Collect;
import com.example.springboot.entity.Goods;
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
    @Autowired
    private AdminMapper adminMapper;   // 新增：用于查询管理员姓名

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
        goods.setAdminId(TokenUtils.getCurrentUser().getId());
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
        return goods;
    }


    @Override
    public IPage<Goods> selectPage(Integer pageNum, Integer pageSize, String name) {
        Page<Goods> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Goods::getName, name);

        Page<Goods> goodsPage = goodsMapper.selectPage(page, queryWrapper);
        goodsPage.getRecords().stream().forEach(goods -> {
            Type type = typeMapper.selectById(goods.getTypeId());
            goods.setTypeName(Objects.nonNull(type) ? type.getName() : "未知类型");

            Admin admin = adminMapper.selectById(goods.getAdminId());
            goods.setAdminName(Objects.nonNull(admin) ? admin.getName() : "未知用户");
        });
        return goodsPage;
    }

    @Override
    public List<Goods> times() {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<Goods>();
        queryWrapper.orderByDesc(Goods::getDate);
        return goodsMapper.selectList(queryWrapper).stream().limit(4).collect(Collectors.toList());
    }

    @Override
    public List<Goods> sales() {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<Goods>();
        queryWrapper.orderByDesc(Goods::getSales);
        return goodsMapper.selectList(queryWrapper).stream().limit(4).collect(Collectors.toList());
    }
    @Override
    public IPage<Goods> selectPageType(Integer pageNum, Integer pageSize, String name, Integer typeId) {
        Page<Goods> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(name), Goods::getName, name);
        queryWrapper.eq(typeId != 0, Goods::getTypeId, typeId);

        Page<Goods> goodsPage = goodsMapper.selectPage(page, queryWrapper);
        goodsPage.getRecords().stream().forEach(goods -> {
            Type type = typeMapper.selectById(goods.getTypeId());
            goods.setTypeName(Objects.nonNull(type) ? type.getName() : "未知类型");

            Admin admin = adminMapper.selectById(goods.getAdminId());
            goods.setAdminName(Objects.nonNull(admin) ? admin.getName() : "未知用户");
        });
        return goodsPage;
    }

}
