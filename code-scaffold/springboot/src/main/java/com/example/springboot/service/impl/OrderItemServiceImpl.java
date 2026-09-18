package com.example.springboot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.OrderItem;
import com.example.springboot.mapper.OrderItemMapper;
import com.example.springboot.service.IOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceImpl implements IOrderItemService {

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Override
    public void save(OrderItem orderItem) {
        orderItemMapper.insert(orderItem);
    }

    @Override
    public void update(OrderItem orderItem) {
        orderItemMapper.updateById(orderItem);
    }

    @Override
    public void remove(Integer id) {
        orderItemMapper.deleteById(id);
    }

    @Override
    public List<OrderItem> selectAll() {
        return orderItemMapper.selectList(null);
    }

    @Override
    public OrderItem selectById(Integer id) {
        return orderItemMapper.selectById(id);
    }

    @Override
    public IPage<OrderItem> selectPage(Integer pageNum, Integer pageSize, String name) {
        Page<OrderItem> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<OrderItem> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(OrderItem::getGoodsName, name);
        return orderItemMapper.selectPage(page, queryWrapper);
    }
}