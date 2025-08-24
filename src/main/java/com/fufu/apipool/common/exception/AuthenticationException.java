package com.fufu.apipool.common.exception;

/**
 * 认证异常类
 * 用于处理认证相关的异常
 * @author lizelin
 */
public class AuthenticationException extends BusinessException {
    
    public AuthenticationException(String message) {
        super("AUTH_ERROR", message);
    }
    
    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("用户名或密码错误");
    }
    
    public static AuthenticationException accountDisabled() {
        return new AuthenticationException("账号已被禁用");
    }
    
    public static AuthenticationException userNotFound() {
        return new AuthenticationException("用户不存在");
    }
}