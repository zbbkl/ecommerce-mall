package com.example.springboot.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.springboot.entity.Admin;
import com.example.springboot.entity.User;
import com.example.springboot.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

/**
 * JWT 生成与请求上下文解析工具。
 */
@Component
public class TokenUtils {

    /**
     * 管理员令牌签名密钥（/admin/login 签发、JwtInterceptor 校验共用）。
     */
    public static final String ADMIN_TOKEN_SECRET = "hachimiAdminSecret2024";

    private static final int TOKEN_EXPIRE_HOURS = 24;

    private static UserMapper userMapper;

    @Autowired
    public void setUserMapper(UserMapper userMapper) {
        TokenUtils.userMapper = userMapper;
    }

    /**
     * 普通用户 Token，使用用户密码签名，与 JwtInterceptor 的校验规则保持一致。
     */
    public static String createToken(String id, String password) {
        return JWT.create()
                .withAudience(id)
                .withExpiresAt(DateUtil.offsetHour(new Date(), TOKEN_EXPIRE_HOURS))
                .sign(Algorithm.HMAC256(password));
    }

    /**
     * 管理员独立登录入口使用的 Token，角色信息保存在自定义声明中。
     */
    public static String generateToken(Integer id, String role) {
        return JWT.create()
                .withAudience(String.valueOf(id))
                .withClaim("role", role)
                .withExpiresAt(DateUtil.offsetHour(new Date(), TOKEN_EXPIRE_HOURS))
                .sign(Algorithm.HMAC256(ADMIN_TOKEN_SECRET));
    }

    public static User getCurrentUser() {
        DecodedJWT jwt = getDecodedToken();
        if (jwt == null || jwt.getAudience().isEmpty()) {
            return null;
        }

        try {
            Integer userId = Integer.valueOf(jwt.getAudience().get(0));
            if (userMapper != null) {
                User user = userMapper.selectById(userId);
                if (user != null) {
                    return user;
                }
            }

            User user = new User();
            user.setId(userId);
            user.setRole(jwt.getClaim("role").asString());
            return user;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static Admin getCurrentAdmin() {
        DecodedJWT jwt = getDecodedToken();
        if (jwt == null || !"ADMIN".equals(jwt.getClaim("role").asString())
                || jwt.getAudience().isEmpty()) {
            return null;
        }

        try {
            Admin admin = new Admin();
            admin.setId(Integer.valueOf(jwt.getAudience().get(0)));
            return admin;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static DecodedJWT getDecodedToken() {
        String token = getTokenFromRequest();
        if (StrUtil.isBlank(token)) {
            return null;
        }

        try {
            return JWT.decode(token);
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static String getTokenFromRequest() {
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("token");
        return StrUtil.isBlank(token) ? request.getParameter("token") : token;
    }
}
