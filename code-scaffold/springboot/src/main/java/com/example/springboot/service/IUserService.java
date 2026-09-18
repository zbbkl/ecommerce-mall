package com.example.springboot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.User;

public interface IUserService extends IService<User> {

    User login(User user);

    User register(User user);

    void resetPassword(User user);

    void updatePassword(User user);

    /**
     * 充值：给当前登录用户加余额（amount 必须为正数）
     */
    void recharge(java.math.BigDecimal amount);

}
