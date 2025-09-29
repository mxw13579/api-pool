package com.fufu.apipool.domain.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * 分页请求DTO
 * @author lizelin
 */
@Data
public class PageRequest {
    /**
     * 页码，从1开始
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "每页大小必须大于0")
    @Max(value = 100, message = "每页大小不能超过100")
    private Integer pageSize = 10;

    /**
     * 排序字段
     */
    private String orderBy;

    /**
     * 排序方向：asc, desc
     */
    private String orderDirection = "desc";
}