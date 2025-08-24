package com.fufu.apipool.common.component;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 请求频率限制器
 * 基于内存的简单实现，适用于单机应用
 * @author lizelin
 */
@Slf4j
@Component
public class RateLimiter {
    
    // 存储每个key的请求记录
    private final Map<String, RequestRecord> requestRecords = new ConcurrentHashMap<>();
    
    /**
     * 检查是否允许请求
     * @param key 限制的key (通常是IP地址或用户ID)
     * @param maxRequests 时间窗口内最大请求数
     * @param windowSeconds 时间窗口大小(秒)
     * @return true表示允许请求，false表示被限制
     */
    public boolean isAllowed(String key, int maxRequests, int windowSeconds) {
        LocalDateTime now = LocalDateTime.now();
        
        RequestRecord record = requestRecords.computeIfAbsent(key, k -> new RequestRecord());
        
        synchronized (record) {
            // 清理过期记录
            if (record.getWindowStart() == null || 
                ChronoUnit.SECONDS.between(record.getWindowStart(), now) >= windowSeconds) {
                record.reset(now);
            }
            
            // 检查是否超过限制
            if (record.getRequestCount().get() >= maxRequests) {
                log.warn("请求频率限制触发: key={}, requests={}, limit={}", key, record.getRequestCount().get(), maxRequests);
                return false;
            }
            
            // 增加请求计数
            record.getRequestCount().incrementAndGet();
            return true;
        }
    }
    
    /**
     * 专门用于登录请求的频率限制
     * @param identifier 标识符 (IP地址或用户名)
     * @return true表示允许登录，false表示被限制
     */
    public boolean isLoginAllowed(String identifier) {
        // 登录限制: 每分钟最多5次尝试
        return isAllowed("login:" + identifier, 5, 60);
    }
    
    /**
     * 获取剩余请求次数
     * @param key 限制的key
     * @param maxRequests 最大请求数
     * @return 剩余请求次数
     */
    public int getRemainingRequests(String key, int maxRequests) {
        RequestRecord record = requestRecords.get(key);
        if (record == null) {
            return maxRequests;
        }
        return Math.max(0, maxRequests - record.getRequestCount().get());
    }
    
    /**
     * 清理过期的记录 (定期调用)
     */
    public void cleanup() {
        LocalDateTime now = LocalDateTime.now();
        requestRecords.entrySet().removeIf(entry -> {
            RequestRecord record = entry.getValue();
            return record.getWindowStart() == null || 
                   ChronoUnit.MINUTES.between(record.getWindowStart(), now) > 10; // 清理10分钟前的记录
        });
    }
    
    /**
     * 请求记录内部类
     */
    private static class RequestRecord {
        private LocalDateTime windowStart;
        private final AtomicInteger requestCount = new AtomicInteger(0);
        
        public void reset(LocalDateTime newWindowStart) {
            this.windowStart = newWindowStart;
            this.requestCount.set(0);
        }
        
        public LocalDateTime getWindowStart() {
            return windowStart;
        }
        
        public AtomicInteger getRequestCount() {
            return requestCount;
        }
    }
}