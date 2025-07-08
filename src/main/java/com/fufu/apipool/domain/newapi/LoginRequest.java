package com.fufu.apipool.domain.newapi;

import lombok.Data;

/**
 * new-api登录参数
 * @author lizelin
 */
@Data
public class LoginRequest {
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
}
