package com.example.springboot.common;

import cn.hutool.core.util.StrUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.example.springboot.exception.ServiceException;
import com.example.springboot.mapper.AdminMapper;
import com.example.springboot.mapper.MerchantMapper;
import com.example.springboot.mapper.UserMapper;
import com.example.springboot.utils.TokenUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 认证 + 角色鉴权拦截器。
 *
 * 职责（三角色统一）：
 * 1. 用全局密钥验签（签名 + 有效期），验签失败一律 401；
 * 2. 校验账号在对应角色表中仍然存在（防止注销/删除后的旧 token 继续使用）；
 * 3. 路径级角色控制：/admin/** 仅 ADMIN、/merchant/** 仅 MERCHANT；
 * 4. 验签通过后把登录 ID / 角色写入 request attribute，业务层统一从 TokenUtils 取，
 *    不允许再自行解码 token。
 */
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private MerchantMapper merchantMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 标记了 @AuthAccess 的方法免认证（匿名浏览、文件下载等）
        if (handler instanceof HandlerMethod) {
            AuthAccess annotation = ((HandlerMethod) handler).getMethodAnnotation(AuthAccess.class);
            if (annotation != null) {
                // 免认证 ≠ 忽略登录态：携带合法 token 时仍解析并写入上下文，
                // 供业务层填充个性化字段（如商品详情的 isCollect），解析失败按匿名处理
                tryResolveAnonymous(request);
                return true;
            }
        }

        // 2. 取 token（请求头优先，其次 URL 参数）
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }
        if (StrUtil.isBlank(token)) {
            throw new ServiceException("401", "token验证失败，请重新登录");
        }

        // 3. 全局密钥统一验签（签名 + 有效期）
        DecodedJWT decodedJWT;
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(TokenUtils.getSecret())).build();
            decodedJWT = verifier.verify(token);
        } catch (JWTVerificationException e) {
            throw new ServiceException("401", "token验证失败，请重新登录");
        }

        String role = decodedJWT.getClaim("role").asString();
        Integer loginId;
        try {
            loginId = Integer.valueOf(decodedJWT.getAudience().get(0));
        } catch (RuntimeException e) {
            throw new ServiceException("401", "token验证失败，请重新登录");
        }

        // 4. 确认账号在对应角色表中仍存在
        if (TokenUtils.ROLE_ADMIN.equals(role)) {
            if (adminMapper.selectById(loginId) == null) {
                throw new ServiceException("401", "管理员不存在，请重新登录");
            }
        } else if (TokenUtils.ROLE_MERCHANT.equals(role)) {
            if (merchantMapper.selectById(loginId) == null) {
                throw new ServiceException("401", "商户不存在，请重新登录");
            }
        } else {
            // 未携带合法角色声明的一律按普通用户处理
            role = TokenUtils.ROLE_USER;
            if (userMapper.selectById(loginId) == null) {
                throw new ServiceException("401", "用户不存在，请重新登录");
            }
        }

        // 5. 路径级角色控制
        String path = request.getRequestURI();
        if (path.startsWith("/admin") && !TokenUtils.ROLE_ADMIN.equals(role)) {
            throw new ServiceException("403", "无权限访问");
        }
        if (path.startsWith("/merchant") && !TokenUtils.ROLE_MERCHANT.equals(role)) {
            throw new ServiceException("403", "无权限访问");
        }

        // 5.5 商品/分类/轮播的写操作仅限管理员（商户改自己的商品走 /merchant/goods 专属接口）
        String method = request.getMethod();
        boolean writeOp = "POST".equals(method) || "PUT".equals(method) || "DELETE".equals(method);
        boolean catalogPath = path.startsWith("/goods") || path.startsWith("/type") || path.startsWith("/carousel");
        if (writeOp && catalogPath && !TokenUtils.ROLE_ADMIN.equals(role)) {
            throw new ServiceException("403", "无权限访问");
        }

        // 6. 验签通过，写入请求上下文，业务层从 TokenUtils 取登录态
        request.setAttribute(TokenUtils.ATTR_LOGIN_ID, loginId);
        request.setAttribute(TokenUtils.ATTR_LOGIN_ROLE, role);
        return true;
    }

    /**
     * @AuthAccess 免认证端点的尽力解析：携带合法 token 且账号仍存在时写入登录态，
     * 否则保持匿名。任何失败都不拦截请求（匿名端点本就允许无 token 访问）。
     */
    private void tryResolveAnonymous(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (StrUtil.isBlank(token)) {
            token = request.getParameter("token");
        }
        if (StrUtil.isBlank(token)) {
            return;
        }
        DecodedJWT decodedJWT;
        try {
            decodedJWT = JWT.require(Algorithm.HMAC256(TokenUtils.getSecret())).build().verify(token);
        } catch (JWTVerificationException e) {
            return;
        }
        String role = decodedJWT.getClaim("role").asString();
        Integer loginId;
        try {
            loginId = Integer.valueOf(decodedJWT.getAudience().get(0));
        } catch (RuntimeException e) {
            return;
        }
        boolean exists;
        if (TokenUtils.ROLE_ADMIN.equals(role)) {
            exists = adminMapper.selectById(loginId) != null;
        } else if (TokenUtils.ROLE_MERCHANT.equals(role)) {
            exists = merchantMapper.selectById(loginId) != null;
        } else {
            role = TokenUtils.ROLE_USER;
            exists = userMapper.selectById(loginId) != null;
        }
        if (!exists) {
            return;
        }
        request.setAttribute(TokenUtils.ATTR_LOGIN_ID, loginId);
        request.setAttribute(TokenUtils.ATTR_LOGIN_ROLE, role);
    }
}
