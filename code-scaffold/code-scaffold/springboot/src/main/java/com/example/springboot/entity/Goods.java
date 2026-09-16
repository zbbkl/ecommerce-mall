package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("goods")
public class Goods {

    @TableId(type= IdType.AUTO)
    private Integer id;
    private String name;
    private String descr;
    private String content;
    private String cover;
    private Double price;
    private Integer store;
    //private Integer userId;
    private Integer adminId;     // 录入管理员ID（代录/录入人）
    private Integer merchantId;  // 归属商户ID（商品所有权）
    private String date;
    private Integer typeId;
    private String state;
    private Integer sales;

    @TableField(exist = false)
    private String typeName;

    @TableField(exist = false)
    private String adminName;

    @TableField(exist = false)
    private String merchantName;   // 归属商户（店铺名称）

    @TableField(exist = false)
    private Boolean isCollect;

}