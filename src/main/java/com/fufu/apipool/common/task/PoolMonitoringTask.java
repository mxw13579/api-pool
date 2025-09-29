package com.fufu.apipool.common.task;

import com.fufu.apipool.entity.PoolEntity;
import com.fufu.apipool.service.PoolService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.Map;

@Slf4j
@Component
@AllArgsConstructor
public class PoolMonitoringTask {

    private final PoolService poolService;

    // 跟踪每个号池的上一次执行时间
    private final Map<Long, Long> lastExecutionTimes = new ConcurrentHashMap<>();

    // 跟踪正在执行的监控任务，防止重复执行
    private final Map<Long, CompletableFuture<Void>> runningTasks = new ConcurrentHashMap<>();

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

            Long poolId = pool.getId();

            // 检查是否已有任务在运行
            CompletableFuture<Void> existingTask = runningTasks.get(poolId);
            if (existingTask != null && !existingTask.isDone()) {
                log.debug("号池 '{}' (ID: {}) 的监控任务正在执行中，跳过本次调度", pool.getName(), poolId);
                continue;
            }

            // 计算下次应执行的时间
            long lastExecution = lastExecutionTimes.getOrDefault(poolId, 0L);
            long intervalMillis = pool.getMonitoringIntervalTime() * 60000L;

            if (now - lastExecution >= intervalMillis) {
                log.info("号池 '{}' (ID: {}) 满足监控条件，开始执行监控任务", pool.getName(), poolId);

                // 异步执行监控任务，避免阻塞调度线程
                CompletableFuture<Void> monitorTask = executePoolMonitoringAsync(pool);
                runningTasks.put(poolId, monitorTask);

                // 更新执行时间
                lastExecutionTimes.put(poolId, now);
            }
        }

        // 清理已完成的任务
        cleanupCompletedTasks();
    }

    /**
     * 异步执行号池监控任务
     */
    @Async("asyncTaskExecutor")
    public CompletableFuture<Void> executePoolMonitoringAsync(PoolEntity pool) {
        return CompletableFuture.runAsync(() -> {
            try {
                poolService.monitorPool(pool.getId());
                log.debug("号池 '{}' (ID: {}) 监控任务执行完成", pool.getName(), pool.getId());
            } catch (Exception e) {
                log.error("执行号池 '{}' (ID: {}) 监控任务时发生异常", pool.getName(), pool.getId(), e);
            }
        });
    }

    /**
     * 清理已完成的任务，避免内存泄漏
     */
    private void cleanupCompletedTasks() {
        runningTasks.entrySet().removeIf(entry -> entry.getValue().isDone());
    }
}
