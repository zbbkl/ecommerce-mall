package com.example.springboot.entity;

import lombok.Data;

/**
 * 结算请求项（非表字段）。
 * 购物车数据保存在前端本地（localStorage），不再入库；结算时前端把选中的商品和数量提交到服务端。
 */
@Data
public class SettleItem {

    private Integer goodsId;

    private Integer nums;
}
