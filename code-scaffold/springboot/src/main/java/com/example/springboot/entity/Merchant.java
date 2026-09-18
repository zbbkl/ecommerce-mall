package com.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 商户。与 admin 对称：独立表 + 独立登录入口 /merchant/login，
 * 全局密钥签发 token，role 声明为 MERCHANT。
 */
@Data
@TableName("merchant")
public class Merchant {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String username;
    private String password;
    private String shopName;      // 店铺名称
    private String logo;          // 店铺LOGO
    private String descr;         // 店铺简介
    private String phone;         // 联系电话
    private String address;       // 经营地址
    private String license;       // 营业执照图片
    private String state;         // 入驻状态：待审核/已通过/已驳回/已停用
    private String rejectReason;  // 驳回原因
    private Double account;       // 可结算余额（台账）
    private String createTime;    // 入驻申请时间

    @TableField(exist = false)
    private String token;

    @TableField(exist = false)
    private String newPassword;
}
