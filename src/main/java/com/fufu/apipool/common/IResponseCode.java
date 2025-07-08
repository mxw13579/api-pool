package com.fufu.apipool.common;

/**
 * 响应错误信息
 * @author lizelin
 */
public interface IResponseCode {

    /**
     * 返回码
     *
     * @return 返回码
     */
    String getCode();

    /**
     * 返回提示信息
     *
     * @return 提示信息
     */
    String getMessage();

    /**
     * 是否成功
     *
     * @return 是否成功
     */
    boolean isSuccess();

}
