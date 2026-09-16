package com.example.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboot.entity.Orders;

import java.util.List;

public interface IOrdersService {

    /**
     * 结算：按商品归属商户拆单（跨商户购物车一次结算生成多张订单，共用同一 parent_no）。
     * 服务端按商品价格重算金额、原子扣减库存；返回结算批次号 parentNo。
     */
    String settle(List<com.example.springboot.entity.SettleItem> items);

    /**
     * 新增
     */
    void save(Orders orders);

    /**
     * 修改
     */
    void update(Orders orders);

    /**
     * 删除（用户仅能删自己的订单）
     */
    void remove(Integer id);

    /**
     * 查询全部数据
     */
    List<Orders> selectAll();

    /**
     * 根据ID查询
     */
    Orders selectById(Integer id);

    /**
     * 分页查询（按登录角色隔离：USER 本人 / ADMIN 全部）
     */
    IPage<Orders> selectPage(Integer pageNum, Integer pageSize, String name, String orderNo, String state);

    /**
     * 当前用户某个状态的订单数量（传空串则统计全部），用于主界面角标
     */
    Long countByState(String state);

    /**
     * 支付单张订单（金额取服务端数据，校验归属与状态）
     */
    void pay(Integer orderId);

    /**
     * 按结算批次批量支付（跨商户拆单后，用户一次付清同批次全部订单）
     */
    void payBatch(String parentNo);

    /**
     * 取消订单（仅待付款可取消，回补库存与销量）
     */
    void cancel(Integer orderId);
}
