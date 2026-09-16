package com.example.springboot.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springboot.entity.Admin;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.AdminMapper;
import com.example.springboot.service.IAdminService;
import com.example.springboot.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements IAdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Override
    public void save(Admin admin) {
        if (StrUtil.isBlank(admin.getPassword())) {
            admin.setPassword("123");
        }
        // 新增管理员一律 BCrypt 存储
        admin.setPassword(PasswordUtils.encode(admin.getPassword()));
        adminMapper.insert(admin);
    }

    @Override
    public void update(Admin admin) {
        // 密码为空则不动原密码（前端编辑页不传密码）
        if (StrUtil.isNotBlank(admin.getPassword())) {
            admin.setPassword(PasswordUtils.encode(admin.getPassword()));
        }
        adminMapper.updateById(admin);
    }

    @Override
    public void remove(Integer id) {
        adminMapper.deleteById(id);
    }

    @Override
    public List<Admin> selectAll() {
        List<Admin> admins = adminMapper.selectList(null);
        admins.forEach(a -> a.setPassword(null));
        return admins;
    }

    @Override
    public Admin selectById(Integer id) {
        Admin admin = adminMapper.selectById(id);
        if (admin != null) {
            admin.setPassword(null);
        }
        return admin;
    }

    @Override
    public IPage<Admin> selectPage(Integer pageNum, Integer pageSize, String username, String name) {
        Page<Admin> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(username), Admin::getUsername, username);
        queryWrapper.like(StrUtil.isNotBlank(name), Admin::getName, name);
        queryWrapper.orderByDesc(Admin::getId);
        IPage<Admin> result = adminMapper.selectPage(page, queryWrapper);
        result.getRecords().forEach(a -> a.setPassword(null));
        return result;
    }

    @Override
    public Admin login(String username, String password) {
        Admin dbAdmin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>().eq(Admin::getUsername, username));
        // BCrypt 校验（存量明文密码兼容，命中后静默升级为散列）
        if (dbAdmin == null || !PasswordUtils.matches(password, dbAdmin.getPassword())) {
            return null;
        }
        if (PasswordUtils.needUpgrade(dbAdmin.getPassword())) {
            Admin upgrade = new Admin();
            upgrade.setId(dbAdmin.getId());
            upgrade.setPassword(PasswordUtils.encode(password));
            adminMapper.updateById(upgrade);
        }
        dbAdmin.setPassword(null);
        return dbAdmin;
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
        if (!PasswordUtils.matches(oldPassword, dbAdmin.getPassword())) {
            throw new ServiceException("原始密码错误");
        }
        Admin update = new Admin();
        update.setId(dbAdmin.getId());
        update.setPassword(PasswordUtils.encode(newPassword));
        adminMapper.updateById(update);
    }
}
