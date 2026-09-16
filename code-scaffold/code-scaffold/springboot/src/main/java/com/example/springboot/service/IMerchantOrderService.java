package com.example.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboot.entity.Orders;

import java.util.Map;

/**
 * 商户侧订单与经营概览业务：全部强制限定「当前登录商户」的数据范围。
 */
public interface IMerchantOrderService {

    /** 商户自己的订单分页（orderNo 模糊 + state 过滤），带明细与买家信息 */
    IPage<Orders> selectPage(Integer pageNum, Integer pageSize, String orderNo, String state);

    /** 商户订单详情（校验归属，带明细） */
    Orders detail(Integer id);

    /** 商户发货：已支付 → 已发货（专用状态流转接口） */
    void ship(Integer id);

    /** 商户经营概览：待发货/待付款/今日订单/今日销售额/库存预警/在售商品数 */
    Map<String, Object> stats();
}
