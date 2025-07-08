package com.fufu.apipool.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 基础实体
 * @author lizelin
 */
@Data
public class BaseEntity implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long createdAt;
    private Long updatedAt;
    private Long deletedAt;
    private String createdBy;
    private String updatedBy;
    private String deletedBy;
}
