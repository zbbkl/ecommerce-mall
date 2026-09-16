package com.example.springboot.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.OrderItem;
import com.example.springboot.entity.Orders;
import com.example.springboot.entity.SettleItem;
import com.example.springboot.entity.Type;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.mapper.OrderItemMapper;
import com.example.springboot.mapper.OrdersMapper;
import com.example.springboot.mapper.TypeMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.IOrdersService;
import com.example.springboot.service.ITypeService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer settle(List<SettleItem> items) {
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser == null || currentUser.getId() == null) {
            throw new ServiceException("401", "请先登录");
        }
        if (items == null || items.isEmpty()) {
            throw new ServiceException("请选择要结算的商品");
        }

        List<OrderItem> orderItems = new ArrayList<>();
        double totalAmount = 0;
        int totalNums = 0;
        Goods mainGoods = null;

        for (SettleItem item : items) {
            Goods goods = goodsMapper.selectById(item.getGoodsId());
            if (goods == null) {
                throw new ServiceException("购物车中有商品已下架，请先移除");
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
            orderItems.add(orderItem);

            totalAmount += goods.getPrice() * nums;
            totalNums += nums;
            if (mainGoods == null) {
                mainGoods = goods;
            }
        }

        Orders orders = new Orders();
        orders.setOrderNo(generateOrderNo());
        orders.setName(orderItems.size() > 1 ? mainGoods.getName() + " 等" + orderItems.size() + "件商品" : mainGoods.getName());
        orders.setGoodsId(mainGoods.getId());   // 主商品，兼容按单个商品展示的既有页面
        orders.setPrice(totalAmount);           // 订单总金额
        orders.setNums(totalNums);              // 商品总件数
        orders.setUserId(currentUser.getId());
        orders.setUserPhone(currentUser.getPhone());
        orders.setUserAddress(currentUser.getAddress());
        orders.setState("待付款");
        orders.setTime(DateUtil.now());
        ordersMapper.insert(orders);

        for (OrderItem orderItem : orderItems) {
            orderItem.setOrderId(orders.getId());
            orderItemMapper.insert(orderItem);
        }

        return orders.getId();
    }

    private String generateOrderNo() {
        String time = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        return time + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    @Override
    public void save(Orders orders) {
        // 1、判断商品库存是否充足，如果不充足，给提示
        Goods goods = goodsMapper.selectById(orders.getGoodsId());
        if (goods == null) {
            throw new ServiceException("商品不存在或已下架");
        }
        int nums = (orders.getNums() == null || orders.getNums() < 1) ? 1 : orders.getNums();
        int store = goods.getStore() == null ? 0 : goods.getStore();
        int sales = goods.getSales() == null ? 0 : goods.getSales();
        // 2、如果商品库存充足，就下单（数据库新增一条订单）
        if (store < nums){
            throw new ServiceException("201", goods.getName() + "商品库存不足");
        }

        // 3、新增订单
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmsss");
        orders.setOrderNo(sdf.format(new Date()));
        orders.setTime(DateUtil.now());
        orders.setNums(nums);
        ordersMapper.insert(orders);

        // 4、商品库存减去对应的数量
        goods.setStore(store - nums);
        // 5、销量累加对应的数量
        goods.setSales(sales + nums);
        // 6、更新一下商品
        goodsMapper.updateById(goods);
    }

    @Override
    public void update(Orders orders) {
        ordersMapper.updateById(orders);
    }

    @Override
    public void remove(Integer id) {
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

        // select * from orders where name like %name% and order_no like %orderNo% and state = xx
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Orders::getName,name);
        queryWrapper.like(Orders::getOrderNo,orderNo);
        queryWrapper.eq(StrUtil.isNotBlank(state), Orders::getState, state);
        queryWrapper.orderByDesc(Orders::getId);   // 最新的订单排在最前面

        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser.getRole().equals("USER")){
            queryWrapper.eq(Orders::getUserId, currentUser.getId());
        }

        Page<Orders> ordersPage = ordersMapper.selectPage(page, queryWrapper);
        ordersPage.getRecords().stream().forEach(orders -> {
            orders.setGoods(goodsMapper.selectById(orders.getGoodsId()));
            orders.setUser(userMapper.selectById(orders.getUserId()));
        });
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

    @Override
    public void pay(Orders orders) {
        // 1、查询登录用户的余额是否充足
        User user = userMapper.selectById(orders.getUserId());
        // 2、如果充足，改变订单状态并且余额减去对应的值
        if (user.getAccount() > orders.getPrice()){
            orders.setState("已支付");
            ordersMapper.updateById(orders);

            // 更新余额
            user.setAccount(user.getAccount() - orders.getPrice());
            userMapper.updateById(user);
        } else{
            // 3、如果余额不足，就给前台提示 ’余额不足，请充值‘
            throw new ServiceException("201", "余额不足，请充值~");
        }
    }

}
