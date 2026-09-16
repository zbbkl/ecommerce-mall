package com.example.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboot.entity.Orders;
import com.example.springboot.entity.SettleItem;
import com.example.springboot.entity.Type;

import java.util.List;

public interface IOrdersService {

    /**
     * 结算：校验库存、原子扣减库存并生成订单，返回订单ID
     */
    Integer settle(List<SettleItem> items);

    /**
     * 新增
     */
    void save(Orders orders);

    /**
     * 修改
     */
    void update(Orders orders);

    /**
     * 删除
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
     * 分页查询
     */
    IPage<Orders> selectPage(Integer pageNum, Integer pageSize, String name, String orderNo, String state);

    /**
     * 当前用户某个状态的订单数量（传空串则统计全部），用于主界面角标
     */
    Long countByState(String state);

    /**
     * 支付功能
     */
    void pay(Orders orders);
}