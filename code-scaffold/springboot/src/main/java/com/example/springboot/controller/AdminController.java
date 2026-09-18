package com.example.springboot.controller;

import cn.hutool.core.util.StrUtil;
import com.example.springboot.common.LoginResponse;
import com.example.springboot.common.Result;
import com.example.springboot.entity.Admin;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.service.IAdminService;
import com.example.springboot.utils.TokenUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IAdminService adminService;

    @PostMapping("/login")
    public Result login(@RequestBody Admin admin) {
        if (StrUtil.isBlank(admin.getUsername()) || StrUtil.isBlank(admin.getPassword())) {
            return Result.error("数据输入不合法");
        }
        Admin loginAdmin = adminService.login(admin.getUsername(), admin.getPassword());
        if (loginAdmin == null) {
            return Result.error("401", "用户名或密码错误");
        }
        // 生成 Token，角色标记为 ADMIN（全局密钥，与用户/商户 token 同一套验签体系）
        String token = TokenUtils.createToken(loginAdmin.getId(), TokenUtils.ROLE_ADMIN);
        loginAdmin.setPassword(null); // 不返回密码
        return Result.success(new LoginResponse(token, loginAdmin));
    }

    /**
     * 新增管理员（用户名唯一，重名返回业务错误）
     */
    @PostMapping
    public Result save(@RequestBody Admin admin) {
        if (StrUtil.isBlank(admin.getUsername())) {
            return Result.error("数据输入不合法");
        }
        try {
            adminService.save(admin);
        } catch (DuplicateKeyException e) {
            return Result.error("用户名已存在");
        }
        return Result.success();
    }

    /**
     * 修改管理员资料（编辑页不传密码时保持原密码不变）
     */
    @PutMapping
    public Result update(@RequestBody Admin admin) {
        adminService.update(admin);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        Admin currentAdmin = TokenUtils.getCurrentAdmin();
        if (currentAdmin == null) {
            throw new ServiceException("401", "token验证失败，请重新登录");
        }
        if (id.equals(currentAdmin.getId())) {
            throw new ServiceException("不能删除当前登录的管理员");
        }
        adminService.remove(id);
        return Result.success();
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        return Result.success(adminService.selectAll());
    }

    @GetMapping("/selectById")
    public Result selectById(@RequestParam Integer id) {
        return Result.success(adminService.selectById(id));
    }

    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "") String username,
                             @RequestParam(defaultValue = "") String name,
                             @RequestParam Integer pageNum,
                             @RequestParam Integer pageSize) {
        return Result.success(adminService.selectPage(pageNum, pageSize, username, name));
    }

    /**
     * 管理员修改自己的密码
     */
    @PostMapping("/password")
    public Result password(@RequestBody Admin admin) {
        adminService.updatePassword(admin.getUsername(), admin.getPassword(), admin.getNewPassword());
        return Result.success();
    }
}
