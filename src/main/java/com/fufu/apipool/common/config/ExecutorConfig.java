package com.fufu.apipool.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author lizelin
 */
@Slf4j
@Configuration
public class ExecutorConfig {
    /**
     * 定义一个专用于监控任务的线程池.
     * 使用 @Bean 注解，Spring会管理这个方法的返回值，并使其成为一个可注入的Bean。
     * Bean的名称默认为方法名，即 "monitoringExecutor". 这正好与 @Qualifier("monitoringExecutor") 匹配。
     *
     * @return ExecutorService 实例
     */
    @Bean(name = "monitoringExecutor")
    public ExecutorService monitoringExecutor() {
        // 获取CPU核心数，用于计算合理的线程数
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        // 最大线程数，可以根据任务的I/O密集程度适当调大
        int maxPoolSize = corePoolSize * 4;

        log.info("初始化线程池 core size: {}, max size:{} ", corePoolSize, maxPoolSize);

        return new ThreadPoolExecutor(
                // 核心线程数：即使空闲也保留的线程数
                corePoolSize,
                // 最大线程数：线程池允许的最大线程数
                maxPoolSize,
                // 空闲线程存活时间：超过核心线程数的空闲线程，在终止前等待新任务的最长时间
                60L,
                // 存活时间单位
                TimeUnit.SECONDS,
                // 任务队列：用于存放等待执行的任务，这里设置了队列容量
                new LinkedBlockingQueue<>(200),
                // 线程工厂：用于创建新线程
                Executors.defaultThreadFactory(),
                // 拒绝策略：当队列满且线程数达到最大时，由提交任务的线程自己执行该任务
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
