package com.fufu.apipool.common.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.io.IOException;

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


    /**
     * 新增：配置资源处理器以解决SPA刷新404问题
     *
     * @param registry 资源处理器注册表
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .resourceChain(true)
                .addResolver(new PathResourceResolver() {
                    @Override
                    protected Resource getResource(String resourcePath, Resource location) throws IOException {
                        Resource requestedResource = location.createRelative(resourcePath);
                        // 如果资源存在或者是API请求，则正常处理
                        // API请求的前缀通常是 /api，根据你的项目进行调整
                        if (requestedResource.exists() && requestedResource.isReadable() || resourcePath.startsWith("/api")) {
                            return requestedResource;
                        }
                        // 否则，返回index.html
                        return location.createRelative("index.html");
                    }
                });
    }
}
