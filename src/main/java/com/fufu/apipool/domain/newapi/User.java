package com.fufu.apipool.domain.newapi;

import lombok.Data;

/**
 * new-api 用户
 *
 * @author lizelin
 */
@Data
public class User {
    /**
     * 用户ID
     */
    public Long id;

    /**
     * 用户名
     */
    public String username;

    /**
     * 密码
     */
    public String password;

    /**
     * 原始密码
     */
    public String originalPassword;

    /**
     * 显示名称
     */
    public String displayName;

    /**
     * 角色
     */
    public Integer role;

    /**
     * 状态
     */
    public Integer status;

    /**
     * 邮箱
     */
    public String email;

    /**
     * GitHub账号ID
     */
    public String githubId;

    /**
     * OIDC账号ID
     */
    public String oidcId;

    /**
     * 微信账号ID
     */
    public String wechatId;

    /**
     * Telegram账号ID
     */
    public String telegramId;

    /**
     * 验证码
     */
    public String verificationCode;

    /**
     * 访问令牌
     */
    public String accessToken;

    /**
     * 配额
     */
    public Integer quota;

    /**
     * 已用配额
     */
    public Integer usedQuota;

    /**
     * 请求次数
     */
    public Integer requestCount;

    /**
     * 用户组
     */
    public String group;

    /**
     * 推广码
     */
    public String affCode;

    /**
     * 推广次数
     */
    public Integer affCount;

    /**
     * 推广配额
     */
    public Integer affQuota;

    /**
     * 推广历史配额
     */
    public Integer affHistoryQuota;

    /**
     * 邀请人ID
     */
    public Long inviterId;

    /**
     * 删除时间
     */
    public Long deletedAt;

    /**
     * Linux DO 账号ID
     */
    public String linuxDoId;

    /**
     * 用户设置
     */
    public String setting;
}
