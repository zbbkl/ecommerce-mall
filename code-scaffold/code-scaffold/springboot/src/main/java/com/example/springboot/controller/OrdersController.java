package com.example.springboot.controller;

import com.example.springboot.common.Result;
import com.example.springboot.entity.Orders;
import com.example.springboot.entity.SettleItem;
import com.example.springboot.entity.Type;
import com.example.springboot.service.IOrdersService;
import com.example.springboot.service.ITypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Autowired
    private IOrdersService ordersService;

    /**
     * 结算（购物车数据保存在前端本地，不再入库）：校验并扣减库存、生成订单，返回订单 ID
     */
    @PostMapping("/settle")
    public Result settle(@RequestBody List<SettleItem> items){
        return Result.success(ordersService.settle(items));
    }

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Orders orders){
        ordersService.save(orders);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Orders orders){
        ordersService.update(orders);
        return Result.success();
    }

    /**
     * 删除
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
     * 分页查询（state 为空串则查全部，支持按订单状态筛选）
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
     * 支付接口
     */
    @PostMapping("/pay")
    public Result pay(@RequestBody Orders orders){
        ordersService.pay(orders);
        return Result.success();
    }
}