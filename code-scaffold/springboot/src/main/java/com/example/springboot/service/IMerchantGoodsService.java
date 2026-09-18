package com.example.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboot.entity.Goods;

/**
 * 商户侧商品业务：所有操作强制限定「当前登录商户」的数据范围（merchant_id 取自登录态）。
 */
public interface IMerchantGoodsService {

    /** 商户自己的商品分页（name 模糊 + state 过滤） */
    IPage<Goods> selectPage(Integer pageNum, Integer pageSize, String name, String state);

    /** 商户新增商品（归属强制为当前商户，需审核已通过） */
    void save(Goods goods);

    /** 商户修改自己的商品（校验归属） */
    void update(Goods goods);

    /** 商户删除自己的商品（校验归属） */
    void remove(Integer id);

    /** 商户上架/下架自己的商品（专用状态接口，不复用整行 update） */
    void updateState(Integer id, String state);
}
