package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.AuthAccess;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Goods;
import com.example.springboot.entity.Merchant;
import com.example.springboot.entity.Type;
import com.example.springboot.mapper.GoodsMapper;
import com.example.springboot.mapper.MerchantMapper;
import com.example.springboot.mapper.TypeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 店铺公开页接口（前台游客可浏览，@AuthAccess 匿名放行）。
 *
 * 只暴露店铺公开信息与上架商品；余额/账号/密码等私有字段一律不返回。
 */
@RestController
@RequestMapping("/merchantShop")
public class MerchantShopController {

    @Autowired
    private MerchantMapper merchantMapper;

    @Autowired
    private GoodsMapper goodsMapper;

    @Autowired
    private TypeMapper typeMapper;

    /**
     * 店铺信息（公开字段）
     */
    @AuthAccess
    @GetMapping("/info")
    public Result info(@RequestParam Integer merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            return Result.error("店铺不存在");
        }
        Merchant shop = new Merchant();
        shop.setId(merchant.getId());
        shop.setShopName(merchant.getShopName());
        shop.setLogo(merchant.getLogo());
        shop.setDescr(merchant.getDescr());
        shop.setState(merchant.getState());
        return Result.success(shop);
    }

    /**
     * 店铺内上架商品分页
     */
    @AuthAccess
    @GetMapping("/goods")
    public Result goods(@RequestParam Integer merchantId,
                        @RequestParam(defaultValue = "1") Integer pageNum,
                        @RequestParam(defaultValue = "12") Integer pageSize) {
        LambdaQueryWrapper<Goods> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Goods::getMerchantId, merchantId);
        queryWrapper.eq(Goods::getState, "上架");
        queryWrapper.orderByDesc(Goods::getId);
        IPage<Goods> page = goodsMapper.selectPage(new Page<>(pageNum, pageSize), queryWrapper);
        page.getRecords().forEach(goods -> {
            Type type = typeMapper.selectById(goods.getTypeId());
            goods.setTypeName(Objects.nonNull(type) ? type.getName() : "未知类型");
            Merchant merchant = merchantMapper.selectById(goods.getMerchantId());
            goods.setMerchantName(Objects.nonNull(merchant) ? merchant.getShopName() : null);
        });
        return Result.success(page);
    }
}
