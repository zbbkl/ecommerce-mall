package com.example.springboot.controller;

import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Goods;
import com.example.springboot.service.IGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商品接口。读操作匿名可浏览（@AuthAccess），写操作需要登录且仅管理员（拦截器控制），
 * 商户端对自己商品的增删改走 /merchant/goods 专属接口。
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    private IGoodsService goodsService;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Goods goods) {
        goodsService.save(goods);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result update(@RequestBody Goods goods) {
        goodsService.update(goods);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        goodsService.remove(id);
        return Result.success();
    }

    /**
     * 查询全部数据
     */
    @AuthAccess
    @GetMapping("/selectAll")
    public Result selectAll() {
        return Result.success(goodsService.selectAll());
    }

    /**
     * 根据ID查询
     */
    @AuthAccess
    @GetMapping("/selectById")
    public Result selectById(@RequestParam Integer id) {
        com.example.springboot.entity.Goods goods = goodsService.selectById(id);
        if (goods == null) {
            return Result.error("商品不存在或已下架");
        }
        return Result.success(goods);
    }

    /**
     * 分页查询
     */
    @AuthAccess
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "") String name,
                             @RequestParam Integer pageNum,
                             @RequestParam Integer pageSize) {
        return Result.success(goodsService.selectPage(pageNum, pageSize, name));
    }
    /**
     * 新品上架
     */
    @AuthAccess
    @GetMapping("/times")
    public Result times() {
        return Result.success(goodsService.times());
    }

    /**
     * 热销商品
     */
    @AuthAccess
    @GetMapping("/sales")
    public Result sales() {
        return Result.success(goodsService.sales());
    }


    /**
     * 分页查询
            */
    @AuthAccess
    @GetMapping("/selectPage/type")
    public Result selectPageType(@RequestParam(defaultValue = "") String name,
                                 @RequestParam Integer typeId,
                                 @RequestParam Integer pageNum,
                                 @RequestParam Integer pageSize) {
        return Result.success(goodsService.selectPageType(pageNum, pageSize, name,typeId));
    }
}


