package com.fufu.apipool.entity;

import com.fufu.apipool.common.BaseEntity;
import lombok.Data;

/**
 * 账号
 * @author lizelin
 */
@Data
public class AccountEntity extends BaseEntity {
    /**
     * 账号名称（昵称）
     */
    private String name;
    /**
     * 登录用户名（唯一）
     */
    private String username;
    /**
     * 登录密码（加密存储）
     */
    private String password;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 账号状态（0-禁用，1-启用）
     */
    private Integer status;
    /**
     * 角色（如admin、user等）
     */
    private String role;
    /**
     * 备注
     */
    private String remark;
    /**
     * 最后登录时间
     */
    private Long lastLoginAt;
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
}
