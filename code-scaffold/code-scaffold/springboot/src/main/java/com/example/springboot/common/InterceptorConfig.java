package com.example.springboot.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 拦截器配置。
 *
 * 放行名单收敛到「真正的匿名入口」（登录/注册/找回密码/商户入驻），
 * 其余接口一律经过 JwtInterceptor 认证 + 角色鉴权；
 * 匿名可浏览的接口（商品列表、分类、轮播图、文件下载）通过 @AuthAccess 标注放行，
 * 不再整段放开业务路径 —— 否则所有商户/订单数据匿名可见。
 *
 * 另：改用 WebMvcConfigurer 接口（原来继承 WebMvcConfigurationSupport 会关掉
 * Spring Boot MVC 自动配置）。
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/login", "/register", "/password",
                        "/admin/login", "/merchant/login", "/merchant/register");
    }

    @Bean
    public JwtInterceptor jwtInterceptor() {
        return new JwtInterceptor();
    }

}
