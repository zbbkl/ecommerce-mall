package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Merchant;
import com.example.springboot.service.IMerchantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 平台侧商户管理（/admin/merchant/**，仅 ADMIN 可访问）：商户列表、入驻审核、停用。
 */
@RestController
@RequestMapping("/admin/merchant")
public class MerchantAdminController {

    @Autowired
    private IMerchantService merchantService;

    /**
     * 商户分页（支持店铺名/状态筛选）
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "") String shopName,
                             @RequestParam(defaultValue = "") String state,
                             @RequestParam Integer pageNum,
                             @RequestParam Integer pageSize) {
        IPage<Merchant> page = merchantService.selectPage(pageNum, pageSize, shopName, state);
        return Result.success(page);
    }

    /**
     * 商户详情
     */
    @GetMapping("/selectById")
    public Result selectById(@RequestParam Integer id) {
        Merchant merchant = merchantService.getById(id);
        if (merchant != null) {
            merchant.setPassword(null);
        }
        return Result.success(merchant);
    }

    /**
     * 审核：state 取 已通过 / 已驳回 / 已停用
     */
    @PutMapping("/audit")
    public Result audit(@RequestBody Merchant merchant) {
        if (merchant.getId() == null || StrUtil.isBlank(merchant.getState())) {
            return Result.error("数据输入不合法");
        }
        merchantService.audit(merchant.getId(), merchant.getState(), merchant.getRejectReason());
        return Result.success();
    }
}
