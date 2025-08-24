package com.fufu.apipool.common.task;

import com.fufu.apipool.common.component.RateLimiter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 安全相关的定时任务
 * @author lizelin
 */
@Slf4j
@Component
@AllArgsConstructor
public class SecurityTask {
    
    private final RateLimiter rateLimiter;
    
    /**
     * 清理过期的频率限制记录
     * 每5分钟执行一次
     */
    @Scheduled(fixedRate = 5 * 60 * 1000) // 5分钟
    public void cleanupRateLimitRecords() {
        try {
            rateLimiter.cleanup();
            log.debug("频率限制记录清理完成");
        } catch (Exception e) {
            log.error("清理频率限制记录时出错", e);
        }
    }
}