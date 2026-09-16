package com.example.springboot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Admin;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.AdminMapper;
import com.example.springboot.service.IAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements IAdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public void save(Admin admin) {
        adminMapper.insert(admin);
    }

    @Override
    public void update(Admin admin) {
        adminMapper.updateById(admin);
    }

    @Override
    public void remove(Integer id) {
        adminMapper.deleteById(id);
    }

    @Override
    public List<Admin> selectAll() {
        return adminMapper.selectList(null);
    }

    @Override
    public Admin selectById(Integer id) {
        return adminMapper.selectById(id);
    }

    @Override
    public IPage<Admin> selectPage(Integer pageNum, Integer pageSize, String username, String name) {
        Page<Admin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(username), Admin::getUsername, username);
        queryWrapper.like(StrUtil.isNotBlank(name), Admin::getName, name);
        queryWrapper.orderByDesc(Admin::getId);
        return adminMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Admin login(String username, String password) {
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getUsername, username);
        queryWrapper.eq(Admin::getPassword, password);
        return adminMapper.selectOne(queryWrapper);
    }

    @Override
    public void updatePassword(String username, String oldPassword, String newPassword) {
        if (StrUtil.isBlank(username) || StrUtil.isBlank(oldPassword) || StrUtil.isBlank(newPassword)) {
            throw new ServiceException("数据输入不合法");
        }
        Admin dbAdmin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        if (dbAdmin == null) {
            throw new ServiceException("管理员不存在");
        }
        if (!oldPassword.equals(dbAdmin.getPassword())) {
            throw new ServiceException("原始密码错误");
        }
        dbAdmin.setPassword(newPassword);
        adminMapper.updateById(dbAdmin);
    }
}