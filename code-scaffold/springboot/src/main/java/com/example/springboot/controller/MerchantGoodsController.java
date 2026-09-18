package com.example.springboot.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Goods;
import com.example.springboot.service.IMerchantGoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 商户侧商品接口（/merchant/goods/**，仅 MERCHANT 可访问，数据范围强制为当前商户）。
 */
@RestController
@RequestMapping("/merchant/goods")
public class MerchantGoodsController {

    @Autowired
    private IMerchantGoodsService merchantGoodsService;

    /**
     * 商户自己的商品分页
     */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "") String name,
                             @RequestParam(defaultValue = "") String state,
                             @RequestParam Integer pageNum,
                             @RequestParam Integer pageSize) {
        IPage<Goods> page = merchantGoodsService.selectPage(pageNum, pageSize, name, state);
        return Result.success(page);
    }

    /**
     * 商户新增商品
     */
    @PostMapping("/add")
    public Result add(@RequestBody Goods goods) {
        merchantGoodsService.save(goods);
        return Result.success();
    }

    /**
     * 商户修改自己的商品
     */
    @PutMapping("/update")
    public Result update(@RequestBody Goods goods) {
        merchantGoodsService.update(goods);
        return Result.success();
    }

    /**
     * 商户删除自己的商品
     */
    @DeleteMapping("/delete")
    public Result delete(@RequestParam Integer id) {
        merchantGoodsService.remove(id);
        return Result.success();
    }

    /**
     * 上架/下架（专用状态接口）
     */
    @PutMapping("/onOff")
    public Result onOff(@RequestParam Integer id, @RequestParam String state) {
        merchantGoodsService.updateState(id, state);
        return Result.success();
    }
}
