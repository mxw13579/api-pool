package com.fufu.apipool.service.impl;

import com.fufu.apipool.entity.ErrorLogEntity;
import com.fufu.apipool.mapper.ErrorLogMapper;
import com.fufu.apipool.service.ErrorLogService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

@Slf4j
@Service
public class ErrorLogServiceImpl implements ErrorLogService {

    private final ErrorLogMapper errorLogMapper;
    private final LinkedBlockingQueue<ErrorLogEntity> logQueue = new LinkedBlockingQueue<>(10000);
    private final ScheduledExecutorService batchExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "error-log-batch-writer");
        t.setDaemon(true);
        return t;
    });

    public ErrorLogServiceImpl(ErrorLogMapper errorLogMapper) {
        this.errorLogMapper = errorLogMapper;
        // 启动批量写入任务
        batchExecutor.scheduleWithFixedDelay(this::processBatchLogs, 5, 5, TimeUnit.SECONDS);
    }

    @Override
    @Async("asyncTaskExecutor")
    public void logError(ErrorLogEntity log) {
        log.setCreatedAt(System.currentTimeMillis() / 1000);

        if (!logQueue.offer(log)) {
            log.warn("错误日志队列已满，丢弃日志: {}", log.getErrorMessage());
            // 队列满时直接写入数据库（降级策略）
            try {
                insertLogSafely(log);
            } catch (Exception e) {
                log.error("直接写入错误日志失败", e);
            }
        }
    }

    /**
     * 批量处理错误日志写入，减少数据库锁竞争
     */
    private void processBatchLogs() {
        if (logQueue.isEmpty()) {
            return;
        }

        List<ErrorLogEntity> batchLogs = new ArrayList<>();
        logQueue.drainTo(batchLogs, 100); // 一次批量处理最多100条

        if (batchLogs.isEmpty()) {
            return;
        }

        try {
            insertBatchLogsSafely(batchLogs);
            log.debug("批量写入 {} 条错误日志", batchLogs.size());
        } catch (Exception e) {
            log.error("批量写入错误日志失败，尝试逐条写入", e);
            // 降级：逐条写入
            for (ErrorLogEntity logEntity : batchLogs) {
                try {
                    insertLogSafely(logEntity);
                } catch (Exception retryException) {
                    log.error("单条错误日志写入失败，日志内容: {}", logEntity.getErrorMessage(), retryException);
                }
            }
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertBatchLogsSafely(List<ErrorLogEntity> logs) {
        for (ErrorLogEntity log : logs) {
            errorLogMapper.insert(log);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void insertLogSafely(ErrorLogEntity log) {
        errorLogMapper.insert(log);
    }

    @Override
    public List<ErrorLogEntity> getErrorsByPoolId(Long poolId) {
        List<ErrorLogEntity> byPoolId = errorLogMapper.findByPoolId(poolId);
        log.info("s:{}",byPoolId);
        return byPoolId;
    }
}
