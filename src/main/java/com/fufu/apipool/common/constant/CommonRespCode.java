package com.fufu.apipool.common.constant;

import com.fufu.apipool.common.IResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lizelin
 */
@AllArgsConstructor
@Getter
public enum CommonRespCode implements IResponseCode {

    /**
     * 成功
     */
    SUCCESS("200", "成功", true),

    /**
     * 非法请求
     */
    BAD_REQUEST("400", "非法请求", false),

    /**
     * 未登录
     */
    UNAUTHORIZED("401", "您还未登录，请先登录", false),

    /**
     * 未授权
     */
    FORBIDDEN("403", "没有权限，请联系管理员授权！", false),

    /**
     * 未找到资源
     */
    NOT_FOUND("404", "未找到资源", false),

    /**
     * 系统异常
     */
    SYSTEM_ERROR("500", "系统繁忙，请稍后再试！", false),

    /**
     * 参数非法
     */
    ILLEGAL_ARGUMENT_PARAMS("illegal_argument_params", "请求参数非法", false);

    /**
     * 响应码
     */
    private final String code;

    /**
     * 响应信息
     */
    private final String message;

    /**
     * 是否成功
     */
    private final boolean success;
}

