package com.example.springboot.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.OrderState;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.Merchant;
import com.example.springboot.entity.OrderItem;
import com.example.springboot.entity.Orders;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.mapper.MerchantMapper;
import com.example.springboot.mapper.OrderItemMapper;
import com.example.springboot.mapper.OrdersMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.IMerchantOrderService;
import com.example.springboot.service.impl.MerchantServiceImpl;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商户侧订单业务实现。
 *
 * 安全约定：merchantId 一律取自登录态；跨商户访问一律 403；
 * 状态流转只走专用接口（ship），不复用整行 update。
 */
@Service
public class MerchantOrderServiceImpl implements IMerchantOrderService {

    /** 库存预警阈值：低于该件数提示补货 */
    private static final int STOCK_ALERT_THRESHOLD = 10;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public IPage<Orders> selectPage(Integer pageNum, Integer pageSize, String orderNo, String state) {
        Merchant current = requireCurrentMerchant();
        Page<Orders> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getMerchantId, current.getId());
        queryWrapper.like(StrUtil.isNotBlank(orderNo), Orders::getOrderNo, orderNo);
        queryWrapper.eq(StrUtil.isNotBlank(state), Orders::getState, state);
        queryWrapper.orderByDesc(Orders::getId);
        Page<Orders> ordersPage = ordersMapper.selectPage(page, queryWrapper);
        ordersPage.getRecords().forEach(this::fillDetail);
        return ordersPage;
    }

    @Override
    public Orders detail(Integer id) {
        Orders orders = requireOwned(id);
        fillDetail(orders);
        return orders;
    }

    @Override
    public void ship(Integer id) {
        Orders orders = requireOwned(id);
        // 状态机校验：只有已支付的订单能发货
        if (!OrderState.PAID.equals(orders.getState())) {
            throw new ServiceException("只有「已支付」状态的订单才能发货，当前状态：" + orders.getState());
        }
        Orders update = new Orders();
        update.setId(id);
        update.setState(OrderState.SHIPPED);
        ordersMapper.updateById(update);
    }

    @Override
    public Map<String, Object> stats() {
        Merchant current = requireCurrentMerchant();
        Map<String, Object> stats = new HashMap<>();

        stats.put("pendingShip", countByState(current.getId(), OrderState.PAID));
        stats.put("pendingPay", countByState(current.getId(), OrderState.PENDING_PAY));
        stats.put("shipped", countByState(current.getId(), OrderState.SHIPPED));
        stats.put("completed", countByState(current.getId(), OrderState.COMPLETED));

        // 今日订单与销售额（time 是 varchar，按前缀匹配当天）
        String today = DateUtil.today();
        LambdaQueryWrapper<Orders> todayWrapper = new LambdaQueryWrapper<>();
        todayWrapper.eq(Orders::getMerchantId, current.getId());
        todayWrapper.likeRight(Orders::getTime, today);
        List<Orders> todayOrders = ordersMapper.selectList(todayWrapper);
        stats.put("todayOrders", todayOrders.size());
        stats.put("todaySales", todayOrders.stream()
                .filter(o -> !OrderState.CANCELLED.equals(o.getState()) && o.getPrice() != null)
                .mapToDouble(Orders::getPrice).sum());

        // 在售商品数 + 库存预警列表
        LambdaQueryWrapper<Goods> goodsWrapper = new LambdaQueryWrapper<>();
        goodsWrapper.eq(Goods::getMerchantId, current.getId());
        List<Goods> myGoods = goodsMapper.selectList(goodsWrapper);
        stats.put("goodsCount", myGoods.size());
        List<Map<String, Object>> alerts = myGoods.stream()
                .filter(g -> g.getStore() != null && g.getStore() < STOCK_ALERT_THRESHOLD)
                .sorted((a, b) -> Integer.compare(a.getStore(), b.getStore()))
                .limit(5)
                .map(g -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", g.getId());
                    item.put("name", g.getName());
                    item.put("store", g.getStore());
                    return item;
                })
                .collect(Collectors.toList());
        stats.put("stockAlerts", alerts);
        return stats;
    }

    // ==================== 内部方法 ====================

    private Merchant requireCurrentMerchant() {
        Merchant current = TokenUtils.getCurrentMerchant();
        if (current == null) {
            throw new ServiceException("401", "请先登录商户账号");
        }
        return current;
    }

    private Orders requireOwned(Integer orderId) {
        Merchant current = requireCurrentMerchant();
        if (orderId == null) {
            throw new ServiceException("参数不合法");
        }
        Orders orders = ordersMapper.selectById(orderId);
        if (orders == null) {
            throw new ServiceException("订单不存在");
        }
        if (!current.getId().equals(orders.getMerchantId())) {
            throw new ServiceException("403", "无权操作其他商户的订单");
        }
        return orders;
    }

    private Long countByState(Integer merchantId, String state) {
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getMerchantId, merchantId);
        queryWrapper.eq(Orders::getState, state);
        return ordersMapper.selectCount(queryWrapper);
    }

    private void fillDetail(Orders orders) {
        // 明细（含商品封面需要前端再查商品，这里带商品ID即可）
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orders.getId());
        orders.setItems(orderItemMapper.selectList(itemWrapper));

        // 买家信息
        if (orders.getUserId() != null) {
            User user = userMapper.selectById(orders.getUserId());
            if (user != null) {
                user.setPassword(null);
                orders.setUser(user);
            }
        }

        // 商户店铺名
        Merchant merchant = merchantMapper.selectById(orders.getMerchantId());
        orders.setMerchantName(merchant == null ? null : merchant.getShopName());

        // 主商品（兼容旧页面按单商品展示）
        if (orders.getGoodsId() != null) {
            orders.setGoods(goodsMapper.selectById(orders.getGoodsId()));
        }
    }
}
