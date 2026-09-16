package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Orders;
import com.example.springboot.entity.SettleItem;
import com.example.springboot.service.IOrdersService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    private IOrdersService ordersService;

    /**
     * 结算（购物车/立即购买统一入口）：
     * 按商品归属商户拆单（同批次共用 parent_no）、原子扣库存、金额服务端重算，返回批次号
     */
    @PostMapping("/settle")
    public Result settle(@RequestBody List<SettleItem> items){
        return Result.success(ordersService.settle(items));
    }

    /**
     * 新增（旧的单商品直接下单入口，保留兼容）
     */
    @PostMapping("/add")
    public Result add(@RequestBody Orders orders){
        ordersService.save(orders);
        return Result.success();
    }

    /**
     * 修改（仅管理员可整体修改；用户侧状态变更走 pay/cancel，商户侧走 ship）
     */
    @PutMapping("/update")
    public Result update(@RequestBody Orders orders){
        if (!TokenUtils.ROLE_ADMIN.equals(TokenUtils.getCurrentRole())) {
            return Result.error("403", "无权限访问");
        }
        ordersService.update(orders);
        return Result.success();
    }

    /**
     * 删除（用户仅能删自己的订单，管理员可删任意订单）
     */
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id){
        ordersService.remove(id);
        return Result.success();
    }

    /**
     * 查询全部数据
     */
    @GetMapping("/selectAll")
    public Result selectAll(){
        return Result.success(ordersService.selectAll());
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById")
    public Result selectById(@RequestParam Integer id){
        return Result.success(ordersService.selectById(id));
    }

    /**
     * 分页查询（state 为空串则查全部，支持按订单状态筛选；USER 只能看自己的订单）
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "") String name,
                             @RequestParam(defaultValue = "") String orderNo,
                             @RequestParam(defaultValue = "") String state,
                             @RequestParam Integer pageNum,
                             @RequestParam Integer pageSize){
        return Result.success(ordersService.selectPage(pageNum,pageSize,name,orderNo,state));
    }

    /**
     * 当前用户某状态的订单数量（主界面角标用）
     */
    @GetMapping("/count")
    public Result count(@RequestParam(defaultValue = "") String state){
        return Result.success(ordersService.countByState(state));
    }

    /**
     * 支付单张订单（金额/归属/状态全部服务端校验，请求体只需传 id）
     */
    @PostMapping("/pay")
    public Result pay(@RequestBody Map<String, Object> body){
        Integer orderId = parseId(body.get("id"));
        if (orderId == null) {
            return Result.error("参数不合法");
        }
        ordersService.pay(orderId);
        return Result.success();
    }

    /**
     * 按结算批次批量支付（跨商户拆单后一次付清同批次订单）
     */
    @PostMapping("/payBatch")
    public Result payBatch(@RequestBody Map<String, Object> body){
        Object parentNo = body.get("parentNo");
        if (parentNo == null) {
            return Result.error("参数不合法");
        }
        ordersService.payBatch(String.valueOf(parentNo));
        return Result.success();
    }

    /**
     * 取消订单（仅待付款可取消，回补库存）
     */
    @PostMapping("/cancel")
    public Result cancel(@RequestBody Map<String, Object> body){
        Integer orderId = parseId(body.get("id"));
        if (orderId == null) {
            return Result.error("参数不合法");
        }
        ordersService.cancel(orderId);
        return Result.success();
    }

    /**
     * 确认收货（仅本人、已发货 → 已完成）
     */
    @PostMapping("/confirm")
    public Result confirm(@RequestBody Map<String, Object> body){
        Integer orderId = parseId(body.get("id"));
        if (orderId == null) {
            return Result.error("参数不合法");
        }
        ordersService.confirm(orderId);
        return Result.success();
    }

    private Integer parseId(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return value == null ? null : Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
