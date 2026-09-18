package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.OrderItem;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.service.IOrderItemService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单明细接口。明细挂在订单下、含成交价，只对管理员开放
 * （用户/商户的明细数据已随 /orders、/merchant/orders 的详情接口返回）。
 */
@RestController
@RequestMapping("/orderItem")
public class OrderItemController {

    @Autowired
    private IOrderItemService orderItemService;

    private void requireAdmin() {
        if (!TokenUtils.ROLE_ADMIN.equals(TokenUtils.getCurrentRole())) {
            throw new ServiceException("403", "无权限访问");
        }
    }

    @PostMapping("/add")
    public Result add(@RequestBody OrderItem orderItem) {
        requireAdmin();
        orderItemService.save(orderItem);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody OrderItem orderItem) {
        requireAdmin();
        orderItemService.update(orderItem);
        return Result.success();
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        requireAdmin();
        orderItemService.remove(id);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        requireAdmin();
        return Result.success(orderItemService.selectAll());
    }

    @GetMapping("/selectById")
    public Result selectById(@RequestParam Integer id) {
        requireAdmin();
        return Result.success(orderItemService.selectById(id));
    }

    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "") String name,
                             @RequestParam Integer pageNum,
                             @RequestParam Integer pageSize) {
        requireAdmin();
        return Result.success(orderItemService.selectPage(pageNum, pageSize, name));
    }
}