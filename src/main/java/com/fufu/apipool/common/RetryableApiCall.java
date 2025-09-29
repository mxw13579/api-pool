package com.fufu.apipool.common;

import com.fufu.apipool.common.exception.ApiAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 重试装饰器
 * 提供API调用失败时的自动重试机制
 * @author lizelin
 */
@Slf4j
@Component
public class RetryableApiCall {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000;

    /**
     * 执行可重试的API调用
     * @param poolId 号池ID
     * @param apiCall API调用函数
     * @param sessionRefresher 会话刷新函数
     * @param <T> 返回类型
     * @return 调用结果
     */
    public <T> T executeWithRetry(Long poolId, Callable<T> apiCall, Runnable sessionRefresher) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return apiCall.call();
            } catch (ApiAuthException e) {
                lastException = e;
                log.warn("API认证失败，尝试第{}次重试 (poolId: {}, error: {})", attempt, poolId, e.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS && e.isAuthExpired()) {
                    try {
                        // 刷新会话
                        sessionRefresher.run();

                        // 等待一段时间后重试
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (Exception refreshException) {
                        log.error("会话刷新失败 (poolId: {}): {}", poolId, refreshException.getMessage());
                        throw new ApiAuthException("会话刷新失败: " + refreshException.getMessage(),
                                                 refreshException, e.getStatusCode(), String.valueOf(poolId));
                    }
                } else {
                    break;
                }
            } catch (Exception e) {
                lastException = e;
                log.error("API调用失败，第{}次尝试 (poolId: {}): {}", attempt, poolId, e.getMessage());

                // 非认证错误，直接抛出
                if (!(e instanceof ApiAuthException)) {
                    throw new RuntimeException("API调用失败: " + e.getMessage(), e);
                }

                break;
            }
        }

        // 所有重试都失败了
        String errorMsg = String.format("API调用失败，已重试%d次 (poolId: %s)", MAX_RETRY_ATTEMPTS, poolId);
        log.error(errorMsg + ", 最后一次错误: {}", lastException.getMessage());
        throw new RuntimeException(errorMsg, lastException);
    }

    /**
     * 执行简单重试（不涉及会话刷新）
     * @param operation 操作函数
     * @param maxRetries 最大重试次数
     * @param delayMs 重试间隔（毫秒）
     * @param <T> 返回类型
     * @return 调用结果
     */
    public <T> T executeSimpleRetry(Callable<T> operation, int maxRetries, long delayMs) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                log.warn("操作失败，第{}次尝试: {}", attempt, e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("重试被中断", ie);
                    }
                }
            }
        }

        throw new RuntimeException("操作失败，已重试" + maxRetries + "次", lastException);
    }
}