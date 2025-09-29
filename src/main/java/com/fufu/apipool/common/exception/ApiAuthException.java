package com.fufu.apipool.common.exception;

/**
 * API认证异常
 * 用于处理API调用时的认证失败场景
 * @author lizelin
 */
public class ApiAuthException extends RuntimeException {

    private final int statusCode;
    private final String poolId;

    public ApiAuthException(String message) {
        super(message);
        this.statusCode = 401;
        this.poolId = null;
    }

    public ApiAuthException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.poolId = null;
    }

    public ApiAuthException(String message, int statusCode, String poolId) {
        super(message);
        this.statusCode = statusCode;
        this.poolId = poolId;
    }

    public ApiAuthException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 401;
        this.poolId = null;
    }

    public ApiAuthException(String message, Throwable cause, int statusCode, String poolId) {
        super(message, cause);
        this.statusCode = statusCode;
        this.poolId = poolId;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getPoolId() {
        return poolId;
    }

    /**
     * 判断是否为认证失效错误
     */
    public boolean isAuthExpired() {
        return statusCode == 401 || statusCode == 403;
    }
}