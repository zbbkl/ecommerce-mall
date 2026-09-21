package com.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.OrderItem;
import com.example.springboot.entity.Orders;
import com.example.springboot.entity.SettleItem;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.mapper.OrderItemMapper;
import com.example.springboot.mapper.OrdersMapper;
import com.example.springboot.utils.TokenUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单结算（拆单版）集成测试（连接本地真实库 bil_mall）。
 *
 * 覆盖核心安全语义：金额服务端重算、库存原子扣减（不超卖）、
 * 拆单字段（merchantId/parentNo）、取消订单库存回补。
 *
 * 数据管理：每个用例自建测试商品（名称带 __test_ 前缀），测试后删除
 * 订单/明细/商品，不留垃圾数据。settle 不动用户余额（支付才扣），无资金风险。
 */
@SpringBootTest
@DisplayName("订单结算（拆单/库存/取消回补）")
class OrdersServiceIntegrationTest {

    private static final Integer TEST_USER_ID = 4;   // 真实测试用户（账号 123）

    @Autowired
    private IOrdersService ordersService;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    /** 测试产生的订单 id，@AfterEach 统一清理 */
    private final List<Integer> createdOrderIds = new java.util.ArrayList<>();
    private final List<Integer> createdGoodsIds = new java.util.ArrayList<>();

    @BeforeEach
    void setUp() {
        mockLogin(TEST_USER_ID, TokenUtils.ROLE_USER);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
        for (Integer orderId : createdOrderIds) {
            LambdaQueryWrapper<OrderItem> iw = new LambdaQueryWrapper<>();
            iw.eq(OrderItem::getOrderId, orderId);
            orderItemMapper.delete(iw);
            ordersMapper.deleteById(orderId);
        }
        for (Integer goodsId : createdGoodsIds) {
            goodsMapper.deleteById(goodsId);
        }
    }

    private void mockLogin(Integer userId, String role) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(TokenUtils.ATTR_LOGIN_ID, userId);
        request.setAttribute(TokenUtils.ATTR_LOGIN_ROLE, role);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    private Goods createTestGoods(double price, int store) {
        Goods goods = new Goods();
        goods.setName("__test_settle_goods__");
        goods.setPrice(price);
        goods.setStore(store);
        goods.setState("上架");
        goods.setMerchantId(1);
        goods.setTypeId(1);
        goods.setSales(0);
        goods.setDate("2026-09-21");
        goodsMapper.insert(goods);
        createdGoodsIds.add(goods.getId());
        return goods;
    }

    private Goods reload(Integer goodsId) {
        return goodsMapper.selectById(goodsId);
    }

    @Test
    @DisplayName("settle：金额服务端重算 + order_item 明细落库 + 拆单字段完整")
    void settleCreatesOrderWithServerSidePrice() {
        Goods goods = createTestGoods(12.5, 100);
        SettleItem item = new SettleItem();
        item.setGoodsId(goods.getId());
        item.setNums(3);

        String parentNo = ordersService.settle(Collections.singletonList(item));

        assertNotNull(parentNo);
        Orders orders = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getParentNo, parentNo));
        assertNotNull(orders, "settle 后应生成订单");
        createdOrderIds.add(orders.getId());

        assertEquals(37.5, orders.getPrice(), "总价必须按服务端商品价重算（12.5×3）");
        assertEquals(3, orders.getNums());
        assertEquals("待付款", orders.getState());
        assertEquals(TEST_USER_ID, orders.getUserId());
        assertEquals(1, orders.getMerchantId());

        LambdaQueryWrapper<OrderItem> iw = new LambdaQueryWrapper<>();
        iw.eq(OrderItem::getOrderId, orders.getId());
        List<OrderItem> items = orderItemMapper.selectList(iw);
        assertEquals(1, items.size(), "应生成 order_item 明细");
        assertEquals(goods.getId(), items.get(0).getGoodsId());
        assertEquals(12.5, items.get(0).getPrice());

        assertEquals(97, reload(goods.getId()).getStore(), "库存应原子扣减 100-3");
    }

    @Test
    @DisplayName("settle：库存不足 → 抛 201 且库存不变、无订单产生")
    void settleRejectsWhenStockInsufficient() {
        Goods goods = createTestGoods(5.0, 1);
        int storeBefore = reload(goods.getId()).getStore();

        SettleItem item = new SettleItem();
        item.setGoodsId(goods.getId());
        item.setNums(2);   // 超过库存 1

        ServiceException e = assertThrows(ServiceException.class,
                () -> ordersService.settle(Collections.singletonList(item)));
        assertEquals("201", e.getCode(), "库存不足语义用 code=201 传递");
        assertTrue(e.getMessage().contains("库存不足"));

        assertEquals(storeBefore, reload(goods.getId()).getStore(), "失败结算不得扣库存");
        assertEquals(0, ordersMapper.selectCount(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getGoodsId, goods.getId())), "失败结算不得产生订单");
    }

    @Test
    @DisplayName("cancel：待付款订单取消 → 状态已取消 + 库存回补")
    void cancelRestoresStock() {
        Goods goods = createTestGoods(7.0, 10);
        SettleItem item = new SettleItem();
        item.setGoodsId(goods.getId());
        item.setNums(4);
        String parentNo = ordersService.settle(Collections.singletonList(item));

        Orders orders = ordersMapper.selectOne(new LambdaQueryWrapper<Orders>()
                .eq(Orders::getParentNo, parentNo));
        assertNotNull(orders);
        createdOrderIds.add(orders.getId());
        assertEquals(6, reload(goods.getId()).getStore(), "结算扣减 10-4");

        ordersService.cancel(orders.getId());

        Orders after = ordersMapper.selectById(orders.getId());
        assertEquals("已取消", after.getState(), "取消后状态应为已取消");
        assertEquals(10, reload(goods.getId()).getStore(), "取消后库存应回补到 10");
    }
}
