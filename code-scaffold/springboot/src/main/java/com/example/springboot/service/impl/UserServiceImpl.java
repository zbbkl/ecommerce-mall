package com.example.springboot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.service.IUserService;
import com.example.springboot.utils.PasswordUtils;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private UserMapper userMapper;


    @Override
    public boolean save(User entity) {
        if (StrUtil.isBlank(entity.getName())) {
            entity.setName(entity.getUsername());
        }
        if (StrUtil.isBlank(entity.getPassword())) {
            entity.setPassword("123");
        }
        if (StrUtil.isBlank(entity.getRole())) {
            entity.setRole("USER");
        }
        // 新增用户一律 BCrypt 存储
        entity.setPassword(PasswordUtils.encode(entity.getPassword()));
        return super.save(entity);
    }

    public User selectByUsername(String username,String role) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);  //  eq => ==   where username = #{username}
        queryWrapper.eq("role", role);
        // 根据用户名查询数据库的用户信息，相当于select * from user where username = #{username}
        return getOne(queryWrapper);
    }

    // 验证用户账户是否合法
    public User login(User user) {
        User dbUser = selectByUsername(user.getUsername(),user.getRole());
        if (dbUser == null) {
            throw new ServiceException("用户名或密码错误");
        }
        // BCrypt 校验（存量明文密码兼容，命中后静默升级为散列）
        if (!PasswordUtils.matches(user.getPassword(), dbUser.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        if (PasswordUtils.needUpgrade(dbUser.getPassword())) {
            User upgrade = new User();
            upgrade.setId(dbUser.getId());
            upgrade.setPassword(PasswordUtils.encode(user.getPassword()));
            updateById(upgrade);
        }
        // 生成token（全局密钥 + role 声明，与拦截器验签规则一致）
        String token = TokenUtils.createToken(dbUser.getId(), TokenUtils.ROLE_USER);
        dbUser.setToken(token);
        dbUser.setPassword(null);
        return dbUser;
    }

    public User register(User user) {
        User dbUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername,user.getUsername()));
        if (dbUser != null) {
            throw new ServiceException("用户名已存在");
        }
        user.setName(user.getUsername());
        user.setPassword(PasswordUtils.encode(user.getPassword()));
        userMapper.insert(user);
        user.setPassword(null);
        return user;
    }

    public void resetPassword(User user) {
        User dbUser = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername,user.getUsername()));
        if (dbUser == null) {
            throw new ServiceException("用户不存在");
        }
        if (!user.getPhone().equals(dbUser.getPhone())) {
            throw new ServiceException("验证错误");
        }
        // 重置为默认密码 123（BCrypt 存储）。注意：用户名+手机号即可重置的弱校验问题仍在，
        // 生产环境必须加验证码/限流（见待修复与待办 P0-5）
        User update = new User();
        update.setId(dbUser.getId());
        update.setPassword(PasswordUtils.encode("123"));
        updateById(update);
    }

    @Override
    public void updatePassword(User user) {
        User dbUser = getById(user.getId());
        if (dbUser == null || !PasswordUtils.matches(user.getPassword(), dbUser.getPassword())) {
            throw new ServiceException("原始密码错误");
        }
        int update = userMapper.updatePassword(user.getId(), PasswordUtils.encode(user.getNewPassword()));
        if (update < 1) {
            throw new ServiceException("原始密码错误");
        }
    }

    @Override
    public void recharge(java.math.BigDecimal amount) {
        // 充值仅限本人；金额必须为正（防负数刷余额）；SQL 原子加防并发丢更新
        User current = com.example.springboot.utils.TokenUtils.getCurrentUser();
        if (current == null || current.getId() == null) {
            throw new ServiceException("401", "请先登录");
        }
        if (amount == null || amount.signum() <= 0 || amount.compareTo(new java.math.BigDecimal("100000")) > 0) {
            throw new ServiceException("充值金额不合法");
        }
        int update = userMapper.recharge(current.getId(), amount);
        if (update < 1) {
            throw new ServiceException("充值失败");
        }
    }
}
