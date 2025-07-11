package com.fufu.apipool.domain.vo;

import com.fufu.apipool.entity.ProxyEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * vo
 * @author lizelin
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProxyVO extends ProxyEntity {

    /**
     * 绑定的号池数量
     */
    private Integer bindCount;

    /**
     * 从实体对象转换
     * @param entity 代理实体
     * @return 代理视图对象
     */
    public static ProxyVO fromEntity(ProxyEntity entity) {
        if (entity == null) {
            return null;
        }
        ProxyVO vo = new ProxyVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setProxyUrl(entity.getProxyUrl());
        vo.setSource(entity.getSource());
        vo.setAddress(entity.getAddress());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        vo.setCreatedBy(entity.getCreatedBy());
        vo.setUpdatedBy(entity.getUpdatedBy());
        // deletedAt, deletedBy 等其他 BaseEntity 字段...
        return vo;
    }
}
