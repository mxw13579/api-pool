package com.fufu.apipool.entity;

import com.fufu.apipool.common.BaseEntity;
import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 账号
 * @author lizelin
 */
@Data
public class AccountEntity extends BaseEntity {
    /**
     * 账号名称（昵称）
     */
    @NotBlank(message = "账号名称不能为空")
    @Size(max = 50, message = "账号名称长度不能超过50个字符")
    private String name;
    
    /**
     * 登录用户名（唯一）
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 30, message = "用户名长度必须在3-30个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]{3,30}$", message = "用户名只能包含字母、数字和下划线")
    private String username;
    
    /**
     * 登录密码（加密存储）
     */
    @Size(min = 6, max = 100, message = "密码长度必须在6-100个字符之间")
    private String password;
    
    /**
     * 邮箱
     */
    @Email(message = "邮箱格式不正确")
    @Size(max = 100, message = "邮箱长度不能超过100个字符")
    private String email;
    
    /**
     * 手机号
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 账号状态（0-禁用，1-启用）
     */
    @NotNull(message = "账号状态不能为空")
    @Min(value = 0, message = "状态值无效")
    @Max(value = 1, message = "状态值无效")
    private Integer status;
    
    /**
     * 角色（如admin、user等）
     */
    @NotBlank(message = "角色不能为空")
    @Size(max = 20, message = "角色长度不能超过20个字符")
    private String role;
    
    /**
     * 备注
     */
    @Size(max = 200, message = "备注长度不能超过200个字符")
    private String remark;
    
    /**
     * 最后登录时间
     */
    private Long lastLoginAt;
    
    /**
     * 最后登录IP
     */
    @Size(max = 45, message = "IP地址长度不能超过45个字符")
    private String lastLoginIp;
}
