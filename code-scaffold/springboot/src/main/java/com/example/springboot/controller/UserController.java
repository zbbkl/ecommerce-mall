package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.common.Result;
import com.example.springboot.entity.User;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.service.IUserService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户管理接口。
 *
 * 权限约定（修复：此前 /user/** 只要求登录，普通用户/商户 token 可改、删、列出任意用户）：
 * - 增/删/查全部/分页：仅 ADMIN（自助注册走 /register，不经过这里）
 * - 改资料/查详情：ADMIN 任意，其他角色仅限本人
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    private boolean isAdmin() {
        return TokenUtils.ROLE_ADMIN.equals(TokenUtils.getCurrentRole());
    }

    private void requireLogin() {
        User current = TokenUtils.getCurrentUser();
        if (current == null || current.getId() == null) {
            throw new ServiceException("401", "请先登录");
        }
    }

    /**
     * 新增（仅管理员；普通用户自助注册走 /register）
     */
    @PostMapping("/add")
    public Result add(@RequestBody User user) {
        if (!isAdmin()) {
            return Result.error("403", "无权限访问");
        }
        try {
            userService.save(user);
        } catch (DuplicateKeyException e) {
            return Result.error("插入数据库错误");
        } catch (Exception e) {
            return Result.error("系统错误");
        }
        return Result.success();
    }

    /**
     * 修改（管理员可改任意用户；其他角色只能改自己，且不能动密码/角色/余额）
     */
    @PutMapping("/update")
    public Result update(@RequestBody User user) {
        if (!isAdmin()) {
            requireLogin();
            User current = TokenUtils.getCurrentUser();
            if (user.getId() == null || !user.getId().equals(current.getId())) {
                return Result.error("403", "只能修改自己的资料");
            }
            // 三个敏感字段不在个人资料修改范围：密码走 /user/password，角色/余额不可自改
            user.setPassword(null);
            user.setRole(null);
            user.setAccount(null);
        }
        userService.updateById(user);
        return Result.success();
    }

    /**
     * 删除（仅管理员）
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        if (!isAdmin()) {
            return Result.error("403", "无权限访问");
        }
        User currentUser = TokenUtils.getCurrentUser();
        if (currentUser != null && id.equals(currentUser.getId())) {
            throw new ServiceException("不能删除当前的用户");
        }
        userService.removeById(id);
        return Result.success();
    }

    /**
     * 查询全部数据（仅管理员）
     */
    @GetMapping("/selectAll")
    public Result selectAll() {
        if (!isAdmin()) {
            return Result.error("403", "无权限访问");
        }
        List<User> userList = userService.list(new QueryWrapper<User>().orderByDesc("id"));  // select * from user order by id desc
        userList.forEach(u -> u.setPassword(null));
        return Result.success(userList);
    }

    /**
     * 获取详情（管理员可查任意；其他角色仅能查自己）
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        if (!isAdmin()) {
            requireLogin();
            User current = TokenUtils.getCurrentUser();
            if (!id.equals(current.getId())) {
                return Result.error("403", "只能查看自己的资料");
            }
        }
        User user = userService.getById(id);
        if (user != null) {
            user.setPassword(null);
        }
        return Result.success(user);
    }


    /**
     * 分页查询（仅管理员）
     */
    @GetMapping("/selectByPage")
    public Result selectByPage(@RequestParam Integer pageNum,
                               @RequestParam Integer pageSize,
                               @RequestParam String username,
                               @RequestParam String role,
                               @RequestParam String name) {
        if (!isAdmin()) {
            return Result.error("403", "无权限访问");
        }
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().orderByDesc(User::getId);  // 默认倒序，让最新的数据在最上面
        queryWrapper.like(StrUtil.isNotBlank(username), User::getUsername, username);
        queryWrapper.like(StrUtil.isNotBlank(name), User::getName, name);
        queryWrapper.eq(StrUtil.isNotBlank(role), User::getRole, role);
        // select * from user where username like '%#{username}%' and name like '%#{name}%'
        Page<User> page = userService.page(new Page<>(pageNum, pageSize), queryWrapper);
        page.getRecords().forEach(u -> u.setPassword(null));
        return Result.success(page);
    }

    /**
     * 修改密码（校验原始密码，仅能改自己的——id 取登录态更稳，但前端传 id 与原密码双重校验）
     */
    @PostMapping("/password")
    public Result password(@RequestBody User user) {
        requireLogin();
        User current = TokenUtils.getCurrentUser();
        if (!isAdmin() && user.getId() != null && !user.getId().equals(current.getId())) {
            return Result.error("403", "只能修改自己的密码");
        }
        if (user.getId() == null) {
            user.setId(current.getId());
        }
        userService.updatePassword(user);
        return Result.success();
    }

    /**
     * 充值：仅本人，服务端在原余额上原子加指定金额（防止客户端直改 account）
     */
    @PutMapping("/recharge")
    public Result recharge(@RequestBody java.util.Map<String, Object> body) {
        requireLogin();
        Object raw = body.get("account");
        java.math.BigDecimal amount;
        try {
            amount = new java.math.BigDecimal(String.valueOf(raw));
        } catch (NumberFormatException e) {
            return Result.error("充值金额不合法");
        }
        userService.recharge(amount);
        return Result.success();
    }
}
