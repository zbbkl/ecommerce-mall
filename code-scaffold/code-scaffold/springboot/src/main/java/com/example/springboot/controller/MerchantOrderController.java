package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Orders;
import com.example.springboot.service.IMerchantOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商户侧订单接口（/merchant/orders/**，仅 MERCHANT 可访问，数据范围强制为当前商户）。
 */
@RestController
@RequestMapping("/merchant/orders")
public class MerchantOrderController {

    @Autowired
    private IMerchantOrderService merchantOrderService;

    /**
     * 商户自己的订单分页（支持订单号/状态筛选）
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "") String orderNo,
                             @RequestParam(defaultValue = "") String state,
                             @RequestParam Integer pageNum,
                             @RequestParam Integer pageSize) {
        IPage<Orders> page = merchantOrderService.selectPage(pageNum, pageSize, orderNo, state);
        return Result.success(page);
    }

    /**
     * 订单详情（带明细与买家信息）
     */
    @GetMapping("/detail")
    public Result detail(@RequestParam Integer id) {
        return Result.success(merchantOrderService.detail(id));
    }

    /**
     * 发货（已支付 → 已发货，专用状态流转接口）
     */
    @PostMapping("/ship")
    public Result ship(@RequestParam Integer id) {
        merchantOrderService.ship(id);
        return Result.success();
    }
}
