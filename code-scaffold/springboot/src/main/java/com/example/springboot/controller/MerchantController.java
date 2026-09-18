package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Merchant;
import com.example.springboot.service.IMerchantOrderService;
import com.example.springboot.service.IMerchantService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 商户端入口：登录 / 入驻 / 店铺资料 / 改密 / 经营概览。
 * 商品与订单的商户侧操作分别在 /merchant/goods、/merchant/orders。
 */
@RestController
@RequestMapping("/merchant")
public class MerchantController {

    @Autowired
    private IMerchantService merchantService;

    @Autowired
    private IMerchantOrderService merchantOrderService;

    /**
     * 商户登录（返回带 token 的商户信息）
     */
    @PostMapping("/login")
    public Result login(@RequestBody Merchant merchant) {
        if (StrUtil.isBlank(merchant.getUsername()) || StrUtil.isBlank(merchant.getPassword())) {
            return Result.error("数据输入不合法");
        }
        return Result.success(merchantService.login(merchant.getUsername(), merchant.getPassword()));
    }

    /**
     * 商户入驻（提交后待平台审核）
     */
    @PostMapping("/register")
    public Result register(@RequestBody Merchant merchant) {
        return Result.success(merchantService.register(merchant));
    }

    /**
     * 当前登录商户的店铺资料
     */
    @GetMapping("/profile")
    public Result profile() {
        Integer currentId = TokenUtils.getCurrentId();
        if (currentId == null) {
            return Result.error("401", "请先登录");
        }
        Merchant current = merchantService.getById(currentId);
        if (current == null) {
            return Result.error("401", "请先登录");
        }
        current.setPassword(null);
        return Result.success(current);
    }

    /**
     * 修改店铺资料（只允许店铺名/LOGO/简介/电话/地址/执照）
     */
    @PutMapping("/profile")
    public Result updateProfile(@RequestBody Merchant merchant) {
        merchantService.updateProfile(merchant);
        return Result.success();
    }

    /**
     * 商户修改自己的密码
     */
    @PostMapping("/password")
    public Result password(@RequestBody Merchant merchant) {
        merchantService.updatePassword(merchant.getUsername(), merchant.getPassword(), merchant.getNewPassword());
        return Result.success();
    }

    /**
     * 经营概览：待发货/待付款/今日订单/今日销售额/库存预警
     */
    @GetMapping("/stats")
    public Result stats() {
        Map<String, Object> stats = merchantOrderService.stats();
        return Result.success(stats);
    }
}
