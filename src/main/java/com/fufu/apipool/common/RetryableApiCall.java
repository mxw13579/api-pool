package com.fufu.apipool.common;

import com.fufu.apipool.common.exception.ApiAuthException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

/**
 * Wrapper that retries API calls when authentication expires.
 */
@Slf4j
@Component
public class RetryableApiCall {

    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1000L;

    /**
     * Execute an API call with retry support when authentication fails.
     */
    public <T> T executeWithRetry(Long poolId, Callable<T> apiCall, Runnable sessionRefresher) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                return apiCall.call();
            } catch (ApiAuthException authException) {
                lastException = authException;
                log.warn("API authentication failure, retry {} (poolId: {}, error: {})",
                        attempt, poolId, authException.getMessage());

                if (attempt < MAX_RETRY_ATTEMPTS && authException.isAuthExpired()) {
                    try {
                        sessionRefresher.run();
                        Thread.sleep(RETRY_DELAY_MS);
                    } catch (Exception refreshException) {
                        log.error("Failed to refresh session (poolId: {}): {}",
                                poolId, refreshException.getMessage());
                        throw new ApiAuthException("Session refresh failed: " + refreshException.getMessage(),
                                refreshException, authException.getStatusCode(), String.valueOf(poolId));
                    }
                } else {
                    break;
                }
            } catch (Exception ex) {
                lastException = ex;
                log.error("API call failed, retry {} (poolId: {}): {}",
                        attempt, poolId, ex.getMessage());

                if (!(ex instanceof ApiAuthException)) {
                    throw new RuntimeException("API call failed: " + ex.getMessage(), ex);
                }

                break;
            }
        }

        if (lastException instanceof ApiAuthException authException) {
            throw authException;
        }

        String errorMsg = String.format("API call failed after %d retries (poolId: %s)",
                MAX_RETRY_ATTEMPTS, poolId);
        log.error("{} , last error: {}", errorMsg,
                lastException != null ? lastException.getMessage() : "unknown");

        if (lastException == null) {
            throw new RuntimeException(errorMsg);
        }

        throw new RuntimeException(errorMsg, lastException);
    }

    /**
     * Execute a simple retry without session refresh support.
     */
    public <T> T executeSimpleRetry(Callable<T> operation, int maxRetries, long delayMs) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                return operation.call();
            } catch (Exception ex) {
                lastException = ex;
                log.warn("Operation failed, retry {}: {}", attempt, ex.getMessage());

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(delayMs);
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", interruptedException);
                    }
                }
            }
        }

        throw new RuntimeException("Operation failed after " + maxRetries + " retries", lastException);
    }
}
