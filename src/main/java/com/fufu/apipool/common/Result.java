package com.fufu.apipool.common;

import com.fufu.apipool.common.constant.CommonRespCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * @author lizelin
 */
@Getter
@Setter
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 5072438822498458374L;

    /**
     * 返回码
     */
    private String code;

    /**
     * 返回提示信息
     */
    private String msg;

    /**
     * 返回的数据结果
     */
    private T data;

    public Result() {
    }

    public Result(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public Result(String code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public boolean isSuccess() {
        return Objects.equals(CommonRespCode.SUCCESS.getCode(), this.code);
    }
}
