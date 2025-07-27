package com.fufu.apipool.entity;

import com.fufu.apipool.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class ErrorLogEntity extends BaseEntity {
    /**
     * 号池ID
     */
    private Long poolId;
    /**
     * 号池名称
     */
    private String poolName;
    /**
     * 渠道ID
     */
    private Long channelId;
    /**
     * 渠道名称
     */
    private String channelName;
    /**
     * 错误信息
     */
    private String errorMessage;
}
