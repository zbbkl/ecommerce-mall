package com.example.springboot.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.Merchant;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.AdminMapper;
import com.example.springboot.mapper.MerchantMapper;
import com.example.springboot.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

/**
 * JWT 生成与登录态解析工具。
 *
 * 身份模型（三角色统一）：
 * 1. 所有角色共用一个全局签名密钥（application.yml 的 jwt.secret），角色放在 token 的 role 声明里；
 * 2. JwtInterceptor 统一验签后，把登录 ID / 角色写入 request attribute；
 * 3. 本类只从 request attribute 读登录态，不再自行 decode token ——
 *    避免出现「拦截器验签、业务层只解码」两套标准（历史上的伪造 token 漏洞正源于此）。
 */
@Component
public class TokenUtils {

    /** 拦截器写入的登录用户 ID 属性名 */
    public static final String ATTR_LOGIN_ID = "LOGIN_ID";
    /** 拦截器写入的登录角色属性名 */
    public static final String ATTR_LOGIN_ROLE = "LOGIN_ROLE";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_USER = "USER";
    public static final String ROLE_MERCHANT = "MERCHANT";

    private static String secret;
    private static int expireHours;

    private static UserMapper userMapper;
    private static AdminMapper adminMapper;
    private static MerchantMapper merchantMapper;

    @Value("${jwt.secret}")
    public void setSecret(String secret) {
        TokenUtils.secret = secret;
    }

    @Value("${jwt.expire-hours:24}")
    public void setExpireHours(int expireHours) {
        TokenUtils.expireHours = expireHours;
    }

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        TokenUtils.userMapper = userMapper;
    }

    @Autowired
    public void setAdminMapper(AdminMapper adminMapper) {
        TokenUtils.adminMapper = adminMapper;
    }

    @Autowired
    public void setMerchantMapper(MerchantMapper merchantMapper) {
        TokenUtils.merchantMapper = merchantMapper;
    }

    /** 全局签名密钥（拦截器验签与签发共用） */
    public static String getSecret() {
        return secret;
    }

    /**
     * 统一签发：audience 存登录 ID，role 声明存角色，全局密钥签名。
     */
    public static String createToken(Integer id, String role) {
        return JWT.create()
                .withAudience(String.valueOf(id))
                .withClaim("role", role)
                .withExpiresAt(DateUtil.offsetHour(new Date(), expireHours))
                .sign(Algorithm.HMAC256(secret));
    }

    /** 当前登录 ID（来自拦截器写入的 request attribute），未登录返回 null */
    public static Integer getCurrentId() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : (Integer) request.getAttribute(ATTR_LOGIN_ID);
    }

    /** 当前登录角色，未登录返回 null */
    public static String getCurrentRole() {
        HttpServletRequest request = currentRequest();
        return request == null ? null : (String) request.getAttribute(ATTR_LOGIN_ROLE);
    }

    /**
     * 当前登录的普通用户。
     * USER 角色 → 返回 user 表完整记录；
     * 其他角色（如管理员代录商品）→ 返回仅含 id/role 的壳对象，兼容既有调用点。
     */
    public static User getCurrentUser() {
        Integer id = getCurrentId();
        if (id == null) {
            return null;
        }
        String role = getCurrentRole();
        if (ROLE_USER.equals(role) && userMapper != null) {
            User user = userMapper.selectById(id);
            if (user != null) {
                return user;
            }
        }
        User shell = new User();
        shell.setId(id);
        shell.setRole(role);
        return shell;
    }

    /** 当前登录管理员（ADMIN 角色），非管理员返回 null */
    public static Admin getCurrentAdmin() {
        if (!ROLE_ADMIN.equals(getCurrentRole())) {
            return null;
        }
        Integer id = getCurrentId();
        if (id == null) {
            return null;
        }
        Admin admin = new Admin();
        admin.setId(id);
        return admin;
    }

    /** 当前登录商户（MERCHANT 角色），返回 merchant 表完整记录，非商户返回 null */
    public static Merchant getCurrentMerchant() {
        if (!ROLE_MERCHANT.equals(getCurrentRole())) {
            return null;
        }
        Integer id = getCurrentId();
        if (id == null || merchantMapper == null) {
            return null;
        }
        return merchantMapper.selectById(id);
    }

    private static HttpServletRequest currentRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes == null ? null : attributes.getRequest();
    }

    /**
     * 兼容保留：解码 token（不做验签！）。仅供日志/调试，业务逻辑严禁用本方法做身份判定。
     */
    @Deprecated
    public static DecodedJWT decodeUnsafe(String token) {
        if (StrUtil.isBlank(token)) {
            return null;
        }
        try {
            return JWT.decode(token);
        } catch (RuntimeException e) {
            return null;
        }
    }
}
