package com.fufu.apipool.domain.dto;

import lombok.Data;

/**
 * @author lizelin
 * @date 2025-07-10 23:32
 */
@Data
public class ProxyBatchesEntity {
    /**
     * 代理名称
     */
    private String prefixName;
    /**
     * 代理 URL
     */
    private String proxyUrlBatches;
    /**
     * 代理来源
     */
    private String source;
    /**
     * 代理地址
     */
    private String address;
    /**
     * 代理状态,启用/禁用
     */
    private Integer status;

}
