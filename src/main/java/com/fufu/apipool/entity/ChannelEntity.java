package com.fufu.apipool.entity;

import com.fufu.apipool.common.BaseEntity;
import com.fufu.apipool.common.constant.ChannelStatus;
import com.fufu.apipool.common.constant.ChannelType;
import com.fufu.apipool.domain.newapi.Channel;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChannelEntity extends BaseEntity {
    /**
     * ID
     */
    private Long id;
    /**
     * 类型
     */
    private ChannelType type;

    /**
     * 密钥
     */
    private String key;

    /**
     * OpenAI组织ID
     */
    private String openaiOrganization;

    /**
     * 测试模型
     */
    private String testModel;

    /**
     * 状态
     */
    private ChannelStatus status;

    /**
     * 名称
     */
    private String name;

    /**
     * 权重
     */
    private Integer weight;

    /**
     * 创建时间（时间戳）
     */
    private Long createdTime;

    /**
     * 创建时间（时间戳）
     */
    private Long createdAt;

    /**
     * 测试时间（时间戳）
     */
    private Long testTime;

    /**
     * 响应时间
     */
    private Integer responseTime;

    /**
     * 基础URL
     */
    private String baseUrl;

    /**
     * 其他信息（JSON字符串）
     */
    private String other;

    /**
     * 余额
     */
    private Integer balance;

    /**
     * 余额更新时间（时间戳）
     */
    private Long balanceUpdatedTime;

    /**
     * 支持的模型列表（逗号分隔）
     */
    private String models;

    /**
     * 分组
     */
    private String channelGroup;

    /**
     * 已用配额
     */
    private Integer usedQuota;

    /**
     * 模型映射
     */
    private String modelMapping;

    /**
     * 状态码映射
     */
    private String statusCodeMapping;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 是否自动封禁
     */
    private Integer autoBan;

    /**
     * 其他附加信息
     */
    private String otherInfo;

    /**
     * 标签
     */
    private String tag;

    /**
     * 设置信息（JSON字符串）
     */
    private String setting;

    /**
     * 参数覆盖（可为null）
     */
    private String paramOverride;
    /**
     * 号池ID
     */
    private Long poolId;

    public ChannelEntity(Channel channel, Long poolId) {
        if (channel == null) {
            throw new IllegalArgumentException("Channel object cannot be null");
        }
        this.setId(channel.getId());
        this.setName(channel.getName());
        this.setType(channel.getType());
        this.setKey(channel.getKey());
        this.setStatus(channel.getStatus());
        this.setPriority(channel.getPriority());
        this.setWeight(channel.getWeight());
        this.setCreatedAt(channel.getCreatedAt());
        this.setTestTime(channel.getTestTime());
        this.setResponseTime(channel.getResponseTime());
        this.setUsedQuota(channel.getUsedQuota());
        this.setBalance(channel.getBalance());
        this.setBalanceUpdatedTime(channel.getBalanceUpdatedTime());
        this.setSetting(channel.getSetting());
        this.setPoolId(poolId);
    }
}
