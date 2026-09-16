package com.example.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.springboot.entity.Admin;

import java.util.List;

public interface IAdminService {
    void save(Admin admin);
    void update(Admin admin);
    void remove(Integer id);
    List<Admin> selectAll();
    Admin selectById(Integer id);
    IPage<Admin> selectPage(Integer pageNum, Integer pageSize, String username, String name);
    Admin login(String username, String password);  // 管理员登录
    void updatePassword(String username, String oldPassword, String newPassword);  // 管理员修改密码
}
