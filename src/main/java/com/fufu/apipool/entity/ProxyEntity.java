package com.fufu.apipool.entity;

import com.fufu.apipool.common.BaseEntity;
import lombok.Data;

/**
 * 代理实体
 * @author lizelin
 */
@Data
public class ProxyEntity extends BaseEntity {
    /**
     * 代理名称
     */
    private String name;
    /**
     * 代理 URL
     */
    private String proxyUrl;
    /**
     * 代理来源
     */
    private String source;
}
