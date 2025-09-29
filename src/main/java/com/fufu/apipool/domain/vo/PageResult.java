package com.fufu.apipool.domain.vo;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 分页结果VO
 * @author lizelin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    /**
     * 数据列表
     */
    private List<T> items;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页大小
     */
    private Integer pageSize;

    /**
     * 总页数
     */
    private Integer totalPages;

    /**
     * 创建分页结果
     * @param items 数据列表
     * @param total 总记录数
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     */
    public static <T> PageResult<T> of(List<T> items, Long total, Integer pageNum, Integer pageSize) {
        int totalPages = (int) Math.ceil((double) total / pageSize);
        return new PageResult<>(items, total, pageNum, pageSize, totalPages);
    }
}