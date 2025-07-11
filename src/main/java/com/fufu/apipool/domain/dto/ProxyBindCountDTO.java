package com.fufu.apipool.domain.dto;

import lombok.Data;

/**
 * 代理绑定数量 DTO
 * 用于 Mybatis 批量查询返回结果
 * @author lizelin
 */
@Data
public class ProxyBindCountDTO {
    /**
     * 代理ID
     */
    private Long proxyId;

    /**
     * 绑定数量
     */
    private Integer count;
}
