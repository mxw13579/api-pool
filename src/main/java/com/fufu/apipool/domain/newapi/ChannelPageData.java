package com.fufu.apipool.domain.newapi;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 分页数据响应实体
 *
 * @author lizelin
 */
@Data
public class ChannelPageData<T> {
    /**
     * 数据项列表
     */
    private List<T> items;
    /**
     * 当前页码
     */
    private Integer page;
    /**
     * 每页数量
     */
    private Integer pageSize;
    /**
     * 总记录数
     */
    private Integer total;
    /**
     * 类型计数，key为类型编号，value为数量
     */
    private Map<String, Integer> typeCounts;
}
