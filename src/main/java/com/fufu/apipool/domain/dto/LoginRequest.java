package com.fufu.apipool.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求DTO
 * @author lizelin
 */
@Data
public class LoginRequest {
    
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度必须在3-30个字符之间")
    private String username;
    
    @NotBlank(message = "密码不能为空")
    @Size(min = 1, max = 100, message = "密码长度不能超过100个字符")
    private String password;
}
