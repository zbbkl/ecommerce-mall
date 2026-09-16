package com.example.springboot.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.springboot.entity.Merchant;

public interface IMerchantService extends IService<Merchant> {

    /** 商户登录（/merchant/login），返回带 token 的商户信息 */
    Merchant login(String username, String password);

    /** 商户入驻（/merchant/register），默认待审核 */
    Merchant register(Merchant merchant);

    /** 商户修改自己店铺资料（只允许资料字段） */
    void updateProfile(Merchant merchant);

    /** 商户修改自己密码 */
    void updatePassword(String username, String oldPassword, String newPassword);

    /** 平台侧分页查询商户 */
    IPage<Merchant> selectPage(Integer pageNum, Integer pageSize, String shopName, String state);

    /** 平台侧审核：已通过 / 已驳回（可带原因） / 已停用 */
    void audit(Integer id, String state, String rejectReason);
}
