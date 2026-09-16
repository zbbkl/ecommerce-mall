package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("order_item")
public class OrderItem {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer orderId;      // 订单ID
    private Integer goodsId;      // 商品ID
    private String goodsName;     // 商品名称
    private Double price;         // 单价
    private Integer nums;         // 数量
}