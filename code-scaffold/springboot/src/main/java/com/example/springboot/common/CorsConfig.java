package com.example.springboot.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

/**
 * 跨域配置类：解决前后端分离架构中，前端页面与后端接口不在同一域名下时的跨域访问问题。
 *
 * 安全约定：允许的来源（Origin）走**域名白名单**，不再使用 "*" 全放开——
 * 白名单通过 application.yml 的 cors.allowed-origins 配置，生产环境用环境变量/
 * 启动参数覆盖（与 jwt.secret 的覆盖方式一致），本地开发默认放行前端 dev server。
 */
@Configuration
public class CorsConfig {

    /** 预检请求（OPTIONS）缓存时长：1 天 */
    private static final long MAX_AGE = 24 * 60 * 60;

    /**
     * 允许跨域的来源白名单。
     * 默认值覆盖本地前端 dev server 的两种写法（localhost / 127.0.0.1）；
     * 生产环境必须通过 cors.allowed-origins 显式配置正式域名。
     */
    @Value("${cors.allowed-origins:http://localhost:8080,http://127.0.0.1:8080}")
    private List<String> allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 1. 允许的来源：仅白名单内的域名（替代原先的 "*" 全放开）
        allowedOrigins.forEach(corsConfiguration::addAllowedOrigin);

        // 2. 允许的请求头（含自定义 token 头）
        corsConfiguration.addAllowedHeader("*");

        // 3. 允许的 HTTP 方法
        corsConfiguration.addAllowedMethod("*");

        // 预检缓存
        corsConfiguration.setMaxAge(MAX_AGE);

        source.registerCorsConfiguration("/**", corsConfiguration);

        return new CorsFilter(source);
    }
}
