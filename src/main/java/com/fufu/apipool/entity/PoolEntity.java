package com.fufu.apipool.entity;

import com.fufu.apipool.common.BaseEntity;
import lombok.Data;

/**
 * 号池 实体
 * @author lizelin
 */
@Data
public class PoolEntity extends BaseEntity {
    /**
     * 号池名称
     */
    private String name;
    /**
     * api endpoint
     */
    private String endpoint;
    /**
     * api 账号
     */
    private String username;
    /**
     * api 密码
     */
    private String password;
    /**
     * 号池所在地址
     */
    private String address;
    /**
     * 监控间隔时间
     */
    private String monitoringIntervalTime;
}
