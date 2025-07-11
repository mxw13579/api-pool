package com.fufu.apipool.domain.dto;

import lombok.Data;

/**
 * 登录
 * @author lizelin
 */
@Data
public class LoginRequest {
    private String username;
    private String password;
}
