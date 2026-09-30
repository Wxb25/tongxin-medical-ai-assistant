package com.tongxin.ai.config;

import com.tongxin.ai.interceptor.AdminAuthInterceptor;
import com.tongxin.ai.interceptor.JwtAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置
 * 注册用户鉴权拦截器（JwtAuthInterceptor）与管理员鉴权拦截器（AdminAuthInterceptor）
 *
 * @author wyq
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtAuthInterceptor jwtAuthInterceptor;
    private final AdminAuthInterceptor adminAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 普通用户鉴权
        registry.addInterceptor(jwtAuthInterceptor)
                .addPathPatterns(
                        "/api/users/me",
                        "/api/appointments/**",
                        "/api/chat/sessions/**",
                        "/api/chat/stream"
                )
                .excludePathPatterns(
                        "/api/users/register",
                        "/api/users/login",
                        "/api/doctors/**",
                        "/api/drugs/**"
                );

        // 管理员鉴权（/api/admin/login 除外）
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns(
                        "/api/admin/**",
                        "/api/knowledge/**"
                )
                .excludePathPatterns(
                        "/api/admin/login"
                );
    }
}
