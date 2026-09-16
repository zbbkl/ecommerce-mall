package com.example.springboot.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.springboot.entity.Merchant;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.MerchantMapper;
import com.example.springboot.service.IMerchantService;
import com.example.springboot.utils.PasswordUtils;
import com.example.springboot.utils.TokenUtils;
import org.springframework.stereotype.Service;

@Service
public class MerchantServiceImpl extends ServiceImpl<MerchantMapper, Merchant> implements IMerchantService {

    public static final String STATE_PENDING = "待审核";
    public static final String STATE_APPROVED = "已通过";
    public static final String STATE_REJECTED = "已驳回";
    public static final String STATE_DISABLED = "已停用";

    @Override
    public Merchant login(String username, String password) {
        Merchant dbMerchant = getOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getUsername, username));
        // BCrypt 校验（存量明文密码兼容，命中后静默升级为散列）
        if (dbMerchant == null || !PasswordUtils.matches(password, dbMerchant.getPassword())) {
            throw new ServiceException("用户名或密码错误");
        }
        if (STATE_DISABLED.equals(dbMerchant.getState())) {
            throw new ServiceException("账号已停用，请联系平台");
        }
        if (PasswordUtils.needUpgrade(dbMerchant.getPassword())) {
            Merchant upgrade = new Merchant();
            upgrade.setId(dbMerchant.getId());
            upgrade.setPassword(PasswordUtils.encode(password));
            updateById(upgrade);
        }
        String token = TokenUtils.createToken(dbMerchant.getId(), TokenUtils.ROLE_MERCHANT);
        dbMerchant.setToken(token);
        dbMerchant.setPassword(null);
        return dbMerchant;
    }

    @Override
    public Merchant register(Merchant merchant) {
        if (StrUtil.isBlank(merchant.getUsername()) || StrUtil.isBlank(merchant.getPassword())
                || StrUtil.isBlank(merchant.getShopName())) {
            throw new ServiceException("账号、密码和店铺名称不能为空");
        }
        if (merchant.getUsername().length() > 20 || merchant.getPassword().length() > 20) {
            throw new ServiceException("数据输入不合法");
        }
        Merchant dbMerchant = getOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getUsername, merchant.getUsername()));
        if (dbMerchant != null) {
            throw new ServiceException("该账号已被注册");
        }
        merchant.setId(null);
        merchant.setState(STATE_PENDING);
        merchant.setAccount(0.0);
        merchant.setPassword(PasswordUtils.encode(merchant.getPassword()));
        merchant.setCreateTime(DateUtil.now());
        save(merchant);
        merchant.setPassword(null);
        return merchant;
    }

    @Override
    public void updateProfile(Merchant merchant) {
        Integer currentId = TokenUtils.getCurrentId();
        if (currentId == null) {
            throw new ServiceException("401", "请先登录");
        }
        // 只允许改资料字段，登录名/状态/余额不在可改范围
        Merchant update = new Merchant();
        update.setId(currentId);
        update.setShopName(merchant.getShopName());
        update.setLogo(merchant.getLogo());
        update.setDescr(merchant.getDescr());
        update.setPhone(merchant.getPhone());
        update.setAddress(merchant.getAddress());
        updateById(update);
    }

    @Override
    public void updatePassword(String username, String oldPassword, String newPassword) {
        Merchant dbMerchant = getOne(new LambdaQueryWrapper<Merchant>().eq(Merchant::getUsername, username));
        if (dbMerchant == null || !PasswordUtils.matches(oldPassword, dbMerchant.getPassword())) {
            throw new ServiceException("原始密码错误");
        }
        Merchant update = new Merchant();
        update.setId(dbMerchant.getId());
        update.setPassword(PasswordUtils.encode(newPassword));
        updateById(update);
    }

    @Override
    public IPage<Merchant> selectPage(Integer pageNum, Integer pageSize, String shopName, String state) {
        Page<Merchant> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Merchant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StrUtil.isNotBlank(shopName), Merchant::getShopName, shopName);
        queryWrapper.eq(StrUtil.isNotBlank(state), Merchant::getState, state);
        queryWrapper.orderByDesc(Merchant::getId);
        IPage<Merchant> result = page(page, queryWrapper);
        result.getRecords().forEach(m -> m.setPassword(null));
        return result;
    }

    @Override
    public void audit(Integer id, String state, String rejectReason) {
        if (!STATE_APPROVED.equals(state) && !STATE_REJECTED.equals(state) && !STATE_DISABLED.equals(state)) {
            throw new ServiceException("非法的审核状态");
        }
        Merchant dbMerchant = getById(id);
        if (dbMerchant == null) {
            throw new ServiceException("商户不存在");
        }
        Merchant update = new Merchant();
        update.setId(id);
        update.setState(state);
        update.setRejectReason(STATE_REJECTED.equals(state) ? rejectReason : null);
        updateById(update);
    }
}
