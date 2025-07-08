package com.fufu.apipool.common;

/**
 * @author lizelin
 */

import com.fufu.apipool.common.constant.CommonRespCode;
import io.micrometer.common.util.StringUtils;

import java.io.Serializable;
import java.util.Objects;

/**
 * <p>
 * 返回结果工具类
 * </P>
 *
 * @author Qizhong.Chi
 * @version 1.0
 * @date 2025/4/11 14:26
 */
@SuppressWarnings("unused")
public class ResultUtil {

    /**
     * 判断结果是否成功
     *
     * @param result 结果信息
     * @param <T>    泛型类
     * @return 返回结果是否成功
     */
    public static <T> boolean isSuccess(Result<T> result) {
        return Objects.nonNull(result) && CommonRespCode.SUCCESS.getCode().equals(result.getCode());
    }

    /**
     * 获取成功Result--data有数据
     *
     * @param data 数据
     * @param <T>  泛型类
     * @return 成功Result对象
     */
    public static <T> Result<T> getSuccessResult(T data) {
        return new Result<>(CommonRespCode.SUCCESS.getCode(), CommonRespCode.SUCCESS.getMessage(), data);
    }

    /**
     * 获取成功Result--data无数据
     *
     * @param <T> 泛型类
     * @return 成功Result对象
     */
    public static <T extends Serializable> Result<T> getSuccessResult() {
        return new Result<>(CommonRespCode.SUCCESS.getCode(), CommonRespCode.SUCCESS.getMessage());
    }

    /**
     * 获取失败结果
     *
     * @param bizCode      业务标识
     * @param responseCode 返回错误码
     * @param <T>          泛型类
     * @return 错误的result对象
     */
    public static <T> Result<T> getFailResult(String bizCode, IResponseCode responseCode) {
        return getFailResult(bizCode, responseCode, null);
    }

    /**
     * 获取失败结果
     *
     * @param bizCode      业务标识
     * @param responseCode 返回错误码
     * @param data         相关数据
     * @param <T>          泛型类
     * @return 错误的result对象
     */
    public static <T> Result<T> getFailResult(String bizCode, IResponseCode responseCode, T data) {
        return new Result<>(getCode(bizCode, responseCode.getCode()), responseCode.getMessage(), data);
    }

    /**
     * 获取失败结果
     *
     * @param bizCode   业务标识
     * @param errorCode 错误码
     * @param <T>       泛型类
     * @return 错误的result对象
     */
    public static <T> Result<T> getFailResult(String bizCode, String errorCode, String message) {
        return new Result<>(getCode(bizCode, errorCode), message);
    }

    /**
     * 获取最终的错误码
     *
     * @param bizCode   业务标识
     * @param errorCode 返回码
     * @return 最终的错误码
     */
    public static String getCode(String bizCode, String errorCode) {
        if (StringUtils.isNotBlank(bizCode)) {
            return bizCode.concat("_").concat(errorCode);
        }
        return errorCode;
    }
}

