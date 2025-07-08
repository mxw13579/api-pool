package com.fufu.apipool.domain.newapi;

import lombok.Data;

/**
 * new-api的返回结果包装类
 * @author lizelin
 */
@Data
public class R<T> {
    /**
     * 是否成功
     */
    private Boolean success;
    /**
     * 错误信息
     */
    private String message;
    /**
     * 数据
     */
    private T data;
}
