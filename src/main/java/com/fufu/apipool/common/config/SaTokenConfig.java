package com.fufu.apipool.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {
    /**
     * 注册 Sa-Token 拦截器
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册一个 StpLogic 的 Sa-Token 拦截器
        registry.addInterceptor(new SaInterceptor(handle -> {
                    // 指定一条 match 规则
                    // 拦截的 path 列表，可以写多个
                    // 检查当前会话是否登录
                    StpUtil.checkLogin();
                })).addPathPatterns("/**")
                // 排除登录接口
                .excludePathPatterns("/api/account/login",
                        "/favicon.ico",
                        "/index.html",
                        "/static/**",
                        "/public/**",
                        "/resources/**",
                        "/webjars/**",
                        "/**/*.js",
                        "/**/*.css",
                        "/**/*.html",
                        "/**/*.png",
                        "/**/*.jpg",
                        "/**/*.jpeg",
                        "/**/*.gif",
                        "/**/*.svg");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 1. 对所有接口（/**）应用此配置
                .allowedOriginPatterns("*") // 2. 允许任何源发起请求（更安全的方式是指定前端地址，如 "http://localhost:5173"）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 3. 允许的请求方法
                .allowCredentials(true) // 4. 允许携带凭证（如 Cookies 或 Authorization Header）
                .allowedHeaders("*") // 5. 允许所有请求头
                .maxAge(3600); // 6. 预检请求（OPTIONS）的缓存时间，单位为秒
    }
}
