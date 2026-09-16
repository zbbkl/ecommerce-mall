package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.OrderItem;
import com.example.springboot.service.IOrderItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orderItem")
public class OrderItemController {

    @Autowired
    private IOrderItemService orderItemService;

    @PostMapping("/add")
    public Result add(@RequestBody OrderItem orderItem) {
        orderItemService.save(orderItem);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody OrderItem orderItem) {
        orderItemService.update(orderItem);
        return Result.success();
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        orderItemService.remove(id);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        return Result.success(orderItemService.selectAll());
    }

    @GetMapping("/selectById")
    public Result selectById(@RequestParam Integer id) {
        return Result.success(orderItemService.selectById(id));
    }

    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "") String name,
                             @RequestParam Integer pageNum,
                             @RequestParam Integer pageSize) {
        return Result.success(orderItemService.selectPage(pageNum, pageSize, name));
    }
}