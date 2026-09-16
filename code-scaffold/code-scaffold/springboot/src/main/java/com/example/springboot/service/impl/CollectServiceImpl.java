package com.example.springboot.service.impl;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Carousel;
import com.example.springboot.entity.Collect;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.CarouselMapper;
import com.example.springboot.mapper.CollectMapper;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.ICarouselService;
import com.example.springboot.service.ICollectService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class CollectServiceImpl implements ICollectService {

    @Autowired
    private CollectMapper collectMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void save(Collect collect) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new ServiceException("401", "请先登录");
        }
        Integer userId = currentUser.getId();
        // 1、判断该用户是否之前收藏过该商品
        // select * from collect where user_id = xx and goods_id = xx
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Collect::getUserId, userId);
        queryWrapper.eq(Collect::getGoodsId,collect.getGoodsId());
        Collect one = collectMapper.selectOne(queryWrapper);
        // 2、如果收藏过就给之前的记录删掉，提示已收藏
        if (Objects.nonNull(one)){
            collectMapper.delete(queryWrapper);
            // 200 除200之外的状态码
            throw new ServiceException("201","已取消收藏");
        }
        // 3、如果没收藏，我们就把这条记录插入到数据库
        collect.setId(null);
        collect.setUserId(userId);
        collect.setTime(DateUtil.now());
        collectMapper.insert(collect);
    }

    @Override
    public void update(Collect collect) {
        collectMapper.updateById(collect);
    }

    @Override
    public void remove(Integer id) {
        Collect collect = collectMapper.selectById(id);
        if (collect == null) {
            return;
        }
        // 用户只能删自己的收藏，管理员可删任意收藏
        if (!TokenUtils.ROLE_ADMIN.equals(TokenUtils.getCurrentRole())) {
            User currentUser = TokenUtils.getCurrentUser();
            if (currentUser == null || !collect.getUserId().equals(currentUser.getId())) {
                throw new ServiceException("403", "无权删除其他用户的收藏");
            }
        }
        collectMapper.deleteById(id);
    }

    @Override
    public List<Collect> selectAll() {
        return collectMapper.selectList(null);
    }

    @Override
    public Collect selectById(Integer id) {
        return collectMapper.selectById(id);
    }

    @Override
    public IPage<Collect> selectPage(Integer pageNum, Integer pageSize, String name) {
        Page<Collect> page = new Page<>(pageNum, pageSize);

        Page<Collect> collectPage = collectMapper.selectPage(page, null);
        collectPage.getRecords().stream().forEach(collect -> {
            User user = userMapper.selectById(collect.getUserId());
            collect.setUserName(user == null ? "未知用户" : user.getName());
            Goods goods = goodsMapper.selectById(collect.getGoodsId());
            collect.setGoodsName(goods == null ? "商品已下架" : goods.getName());
        });
        return collectPage;
    }

    @Override
    public List<Collect> myCollect() {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new ServiceException("401", "请先登录");
        }
        LambdaQueryWrapper<Collect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Collect::getUserId, currentUser.getId());
        List<Collect> collects = collectMapper.selectList(queryWrapper);
        collects.stream().forEach(collect -> {
            collect.setGoods(goodsMapper.selectById(collect.getGoodsId()));
        });
        return collects;
    }

}