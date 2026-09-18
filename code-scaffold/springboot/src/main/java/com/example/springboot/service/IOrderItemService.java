package com.example.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboot.entity.OrderItem;

import java.util.List;

public interface IOrderItemService {
    void save(OrderItem orderItem);
    void update(OrderItem orderItem);
    void remove(Integer id);
    List<OrderItem> selectAll();
    OrderItem selectById(Integer id);
    IPage<OrderItem> selectPage(Integer pageNum, Integer pageSize, String name);
}