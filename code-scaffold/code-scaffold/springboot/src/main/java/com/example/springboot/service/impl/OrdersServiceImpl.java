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
import com.example.springboot.entity.SettleItem;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.mapper.MerchantMapper;
import com.example.springboot.mapper.OrderItemMapper;
import com.example.springboot.mapper.OrdersMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.IOrdersService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class OrdersServiceImpl implements IOrdersService {

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    /**
     * 结算（拆单版）：
     * 1. 登录态取用户，金额一律按数据库商品价重算，库存原子扣减（不超卖）；
     * 2. 商品按 merchant_id 分组，一个商户一张订单，同一批次共用 parent_no；
     * 3. 每张订单独立走「待付款 → 已支付 → 已发货 → 已完成」，互不阻塞。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String settle(List<SettleItem> items) {
        User currentUser = requireCurrentUser();
        if (items == null || items.isEmpty()) {
            throw new ServiceException("请选择要结算的商品");
        }

        // merchantId → 该商户的明细列表（LinkedHashMap 保持稳定顺序）
        Map<Integer, List<OrderItem>> groups = new LinkedHashMap<>();

        for (SettleItem item : items) {
            Goods goods = goodsMapper.selectById(item.getGoodsId());
            if (goods == null || !"上架".equals(goods.getState())) {
                throw new ServiceException("购物车中有商品已下架，请先移除");
            }
            if (goods.getMerchantId() == null) {
                throw new ServiceException(goods.getName() + " 商品归属异常，请联系平台");
            }
            int nums = (item.getNums() == null || item.getNums() < 1) ? 1 : item.getNums();

            // 库存以数据库为准做原子扣减：影响行数为 0 说明库存不足，事务回滚
            if (goodsMapper.deductStore(goods.getId(), nums) == 0) {
                throw new ServiceException("201", goods.getName() + "商品库存不足");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setGoodsId(goods.getId());
            orderItem.setGoodsName(goods.getName());
            orderItem.setPrice(goods.getPrice());   // 单价取服务端商品价格，不信客户端
            orderItem.setNums(nums);
            orderItem.setMerchantId(goods.getMerchantId());
            groups.computeIfAbsent(goods.getMerchantId(), k -> new ArrayList<>()).add(orderItem);
        }

        String parentNo = generateOrderNo();   // 同一次结算共用批次号
        for (Map.Entry<Integer, List<OrderItem>> entry : groups.entrySet()) {
            createOrderForMerchant(currentUser, entry.getKey(), entry.getValue(), parentNo);
        }
        return parentNo;
    }

    /** 为单个商户生成一张订单及其明细 */
    private void createOrderForMerchant(User currentUser, Integer merchantId, List<OrderItem> orderItems, String parentNo) {
        OrderItem main = orderItems.get(0);
        double totalAmount = orderItems.stream().mapToDouble(i -> i.getPrice() * i.getNums()).sum();
        int totalNums = orderItems.stream().mapToInt(OrderItem::getNums).sum();

        Orders orders = new Orders();
        orders.setOrderNo(generateOrderNo());
        orders.setMerchantId(merchantId);
        orders.setParentNo(parentNo);
        orders.setName(orderItems.size() > 1 ? main.getGoodsName() + " 等" + orderItems.size() + "件商品" : main.getGoodsName());
        orders.setGoodsId(main.getGoodsId());   // 主商品，兼容按单个商品展示的既有页面
        orders.setPrice(totalAmount);           // 本单（本商户）总金额
        orders.setNums(totalNums);              // 本单商品总件数
        orders.setUserId(currentUser.getId());
        orders.setUserPhone(currentUser.getPhone());
        orders.setUserAddress(currentUser.getAddress());
        orders.setState(OrderState.PENDING_PAY);
        orders.setTime(DateUtil.now());
        ordersMapper.insert(orders);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(orders.getId());
            orderItemMapper.insert(orderItem);
        }
    }

    private String generateOrderNo() {
        String time = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        return time + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(Orders orders) {
        // 旧的单商品直接下单入口（保留兼容）：金额服务端重算，userId 取登录态
        User currentUser = requireCurrentUser();
        Goods goods = goodsMapper.selectById(orders.getGoodsId());
        if (goods == null || !"上架".equals(goods.getState())) {
            throw new ServiceException("商品不存在或已下架");
        }
        if (goods.getMerchantId() == null) {
            throw new ServiceException(goods.getName() + " 商品归属异常，请联系平台");
        }
        int nums = (orders.getNums() == null || orders.getNums() < 1) ? 1 : orders.getNums();

        if (goodsMapper.deductStore(goods.getId(), nums) == 0) {
            throw new ServiceException("201", goods.getName() + "商品库存不足");
        }

        orders.setId(null);
        orders.setOrderNo(generateOrderNo());
        orders.setParentNo(orders.getOrderNo());
        orders.setMerchantId(goods.getMerchantId());
        orders.setUserId(currentUser.getId());
        orders.setUserPhone(currentUser.getPhone());
        orders.setUserAddress(currentUser.getAddress());
        orders.setPrice(goods.getPrice() * nums);   // 金额服务端重算
        orders.setNums(nums);
        orders.setState(OrderState.PENDING_PAY);
        orders.setTime(DateUtil.now());
        ordersMapper.insert(orders);

        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orders.getId());
        orderItem.setGoodsId(goods.getId());
        orderItem.setGoodsName(goods.getName());
        orderItem.setPrice(goods.getPrice());
        orderItem.setNums(nums);
        orderItem.setMerchantId(goods.getMerchantId());
        orderItemMapper.insert(orderItem);
    }

    @Override
    public void update(Orders orders) {
        // 通用 update 仅保留给管理端兜底使用；状态流转必须走 pay/cancel/ship 专用接口
        ordersMapper.updateById(orders);
    }

    @Override
    public void remove(Integer id) {
        Orders orders = ordersMapper.selectById(id);
        if (orders == null) {
            return;
        }
        User current = TokenUtils.getCurrentUser();
        String role = TokenUtils.getCurrentRole();
        // 用户只能删自己的订单；管理员可删任意订单
        if (!TokenUtils.ROLE_ADMIN.equals(role)
                && (current == null || !orders.getUserId().equals(current.getId()))) {
            throw new ServiceException("403", "无权删除其他用户的订单");
        }
        ordersMapper.deleteById(id);
    }

    @Override
    public List<Orders> selectAll() {
        return ordersMapper.selectList(null);
    }

    @Override
    public Orders selectById(Integer id) {
        return ordersMapper.selectById(id);
    }

    @Override
    public IPage<Orders> selectPage(Integer pageNum, Integer pageSize, String name, String orderNo, String state) {
        Page<Orders> page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Orders::getName, name);
        queryWrapper.like(Orders::getOrderNo, orderNo);
        queryWrapper.eq(StrUtil.isNotBlank(state), Orders::getState, state);
        queryWrapper.orderByDesc(Orders::getId);   // 最新的订单排在最前面

        // 数据范围按登录角色隔离（判空 + 常量在前，修复原 NPE 即绕过的行级隔离）
        User currentUser = TokenUtils.getCurrentUser();
        String role = TokenUtils.getCurrentRole();
        if (currentUser != null && TokenUtils.ROLE_USER.equals(role)) {
            queryWrapper.eq(Orders::getUserId, currentUser.getId());
        }

        Page<Orders> ordersPage = ordersMapper.selectPage(page, queryWrapper);
        ordersPage.getRecords().forEach(this::fillDetail);
        return ordersPage;
    }

    @Override
    public Long countByState(String state) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            return 0L;
        }
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, currentUser.getId());
        queryWrapper.eq(StrUtil.isNotBlank(state), Orders::getState, state);
        return ordersMapper.selectCount(queryWrapper);
    }

    /**
     * 支付单张订单（安全版）：
     * - 订单从数据库取，userId/price 一律不信客户端（修复「负数价格刷钱」漏洞）
     * - 校验订单归属与状态（待付款才能支付，防重复扣款）
     * - 余额判断改为 >=（恰好等于时不再误拒）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pay(Integer orderId) {
        User currentUser = requireCurrentUser();
        Orders orders = requireOwnedPayableOrder(orderId, currentUser);
        doPay(currentUser, List.of(orders));
    }

    /**
     * 按结算批次批量支付：同 parent_no 下属于当前用户、状态为待付款的全部订单一次付清。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void payBatch(String parentNo) {
        User currentUser = requireCurrentUser();
        if (StrUtil.isBlank(parentNo)) {
            throw new ServiceException("参数不合法");
        }
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getParentNo, parentNo);
        queryWrapper.eq(Orders::getUserId, currentUser.getId());
        queryWrapper.eq(Orders::getState, OrderState.PENDING_PAY);
        List<Orders> payable = ordersMapper.selectList(queryWrapper);
        if (payable.isEmpty()) {
            throw new ServiceException("没有可支付的订单");
        }
        doPay(currentUser, payable);
    }

    /** 批量扣款与状态推进（调用方需在事务内） */
    private void doPay(User currentUser, List<Orders> payable) {
        double total = payable.stream().mapToDouble(Orders::getPrice).sum();
        if (total <= 0) {
            throw new ServiceException("订单金额异常");
        }
        User dbUser = userMapper.selectById(currentUser.getId());
        if (dbUser == null) {
            throw new ServiceException("401", "请先登录");
        }
        if (dbUser.getAccount() == null || dbUser.getAccount() < total) {
            throw new ServiceException("201", "余额不足，请充值~");
        }
        dbUser.setAccount(dbUser.getAccount() - total);
        userMapper.updateById(dbUser);

        for (Orders orders : payable) {
            Orders update = new Orders();
            update.setId(orders.getId());
            update.setState(OrderState.PAID);
            ordersMapper.updateById(update);
        }
    }

    /**
     * 取消订单：仅待付款可取消；原子回补库存与销量。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Integer orderId) {
        User currentUser = requireCurrentUser();
        Orders orders = requireOwnedPayableOrder(orderId, currentUser);

        Orders update = new Orders();
        update.setId(orders.getId());
        update.setState(OrderState.CANCELLED);
        ordersMapper.updateById(update);

        // 回补库存：优先按明细回补（拆单订单有多件商品），无明细的老单按主商品回补
        List<OrderItem> items = orderItemMapper.selectList(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, orders.getId()));
        if (items.isEmpty()) {
            if (orders.getGoodsId() != null) {
                goodsMapper.restoreStore(orders.getGoodsId(), orders.getNums());
            }
        } else {
            for (OrderItem item : items) {
                goodsMapper.restoreStore(item.getGoodsId(), item.getNums());
            }
        }
    }

    /**
     * 确认收货：仅本人订单、已发货状态可确认，推进到已完成。
     */
    @Override
    public void confirm(Integer orderId) {
        User currentUser = requireCurrentUser();
        if (orderId == null) {
            throw new ServiceException("参数不合法");
        }
        Orders orders = ordersMapper.selectById(orderId);
        if (orders == null) {
            throw new ServiceException("订单不存在");
        }
        if (!currentUser.getId().equals(orders.getUserId())) {
            throw new ServiceException("403", "无权操作其他用户的订单");
        }
        if (!OrderState.SHIPPED.equals(orders.getState())) {
            throw new ServiceException("只有「已发货」状态的订单才能确认收货");
        }
        Orders update = new Orders();
        update.setId(orders.getId());
        update.setState(OrderState.COMPLETED);
        ordersMapper.updateById(update);
    }

    // ==================== 内部方法 ====================

    private User requireCurrentUser() {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new ServiceException("401", "请先登录");
        }
        return currentUser;
    }

    /** 校验订单归属 + 待付款状态，返回数据库中的订单 */
    private Orders requireOwnedPayableOrder(Integer orderId, User currentUser) {
        if (orderId == null) {
            throw new ServiceException("参数不合法");
        }
        Orders orders = ordersMapper.selectById(orderId);
        if (orders == null) {
            throw new ServiceException("订单不存在");
        }
        if (!currentUser.getId().equals(orders.getUserId())) {
            throw new ServiceException("403", "无权操作其他用户的订单");
        }
        if (!OrderState.PENDING_PAY.equals(orders.getState())) {
            throw new ServiceException("订单当前状态不可支付，请刷新后重试");
        }
        if (orders.getPrice() == null || orders.getPrice() <= 0) {
            throw new ServiceException("订单金额异常");
        }
        return orders;
    }

    private void fillDetail(Orders orders) {
        LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(OrderItem::getOrderId, orders.getId());
        orders.setItems(orderItemMapper.selectList(itemWrapper));

        if (orders.getUserId() != null) {
            User user = userMapper.selectById(orders.getUserId());
            if (user != null) {
                user.setPassword(null);
                orders.setUser(user);
            }
        }

        if (orders.getMerchantId() != null) {
            Merchant merchant = merchantMapper.selectById(orders.getMerchantId());
            orders.setMerchantName(merchant == null ? null : merchant.getShopName());
        }

        if (orders.getGoodsId() != null) {
            orders.setGoods(goodsMapper.selectById(orders.getGoodsId()));
        }
    }
}
