package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.List;

@Data
@TableName("orders")
public class Orders {

    @TableId(type=IdType.AUTO)
    private Integer id;
    private String name;
    private String orderNo;
    private Integer goodsId;
    private Double price;
    private Integer nums;
    //private String userName;
    private String userPhone;
    private String userAddress;
    private String time;
    private String state;
    private Integer userId;
    private Integer merchantId;   // 归属商户ID（拆单后一单一商户）
    private String parentNo;      // 结算批次号（同一次跨商户结算共用）

    @TableField(exist = false)
    private Goods goods;

    @TableField(exist = false)
    private User user;

    @TableField(exist = false)
    private String merchantName;          // 商户店铺名

    @TableField(exist = false)
    private List<OrderItem> items;        // 订单明细
}