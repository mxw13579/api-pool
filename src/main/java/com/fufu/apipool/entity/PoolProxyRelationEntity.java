package com.fufu.apipool.entity;

import com.fufu.apipool.common.BaseEntity;
import lombok.Data;

/**
 * 号池与代理关联关系实体
 *
 * @author lizelin
 */
@Data
public class PoolProxyRelationEntity extends BaseEntity {
    /**
     * 号池ID
     */
    private Long poolId;

    /**
     * 代理ID
     */
    private Long proxyId;
}
