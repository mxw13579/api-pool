package com.fufu.apipool.common.task;

import com.fufu.apipool.entity.PoolEntity;
import com.fufu.apipool.service.PoolService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class PoolMonitoringTask {

    private final PoolService poolService;
    // 使用一个Map来跟踪每个号池的上一次执行时间，以支持动态间隔
    private final Map<Long, Long> lastExecutionTimes = new ConcurrentHashMap<>();

    /**
     * 每分钟检查一次，决定哪些号池需要执行监控
     */
    @Scheduled(fixedRate = 60000) // 60秒
    public void scheduleDynamicPoolMonitoring() {
        log.info("开始扫描所有号池以确定是否需要执行监控...");
        List<PoolEntity> pools = poolService.selectAll();
        long now = System.currentTimeMillis();

        for (PoolEntity pool : pools) {
            // 如果未设置监控间隔，则跳过
            if (pool.getMonitoringIntervalTime() == null || pool.getMonitoringIntervalTime() <= 0) {
                continue;
            }

            // 计算下次应执行的时间
            long lastExecution = lastExecutionTimes.getOrDefault(pool.getId(), 0L);
            long intervalMillis = pool.getMonitoringIntervalTime() * 60000L;

            if (now - lastExecution >= intervalMillis) {
                log.info("号池 '{}' (ID: {}) 满足监控条件，开始执行监控任务。", pool.getName(), pool.getId());
                try {
                    // 异步执行，避免阻塞调度器线程
                    poolService.monitorPool(pool.getId());
                    lastExecutionTimes.put(pool.getId(), now);
                } catch (Exception e) {
                    log.error("执行号池 '{}' (ID: {}) 监控任务时发生异常。", pool.getName(), pool.getId(), e);
                }
            }
        }
    }
}
