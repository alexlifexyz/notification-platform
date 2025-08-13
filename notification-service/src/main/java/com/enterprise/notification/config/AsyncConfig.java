package com.enterprise.notification.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步配置类
 * 配置通知发送的异步线程池
 *
 * @author Enterprise Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableAsync
public class AsyncConfig {

    @Value("${notification.async.core-pool-size:5}")
    private int corePoolSize;

    @Value("${notification.async.max-pool-size:20}")
    private int maxPoolSize;

    @Value("${notification.async.queue-capacity:100}")
    private int queueCapacity;

    @Value("${notification.async.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${notification.async.thread-name-prefix:NotificationAsync-}")
    private String threadNamePrefix;

    /**
     * 通知异步执行器
     * 专门用于异步发送通知
     */
    @Bean(name = "notificationAsyncExecutor")
    public Executor notificationAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程数
        executor.setCorePoolSize(corePoolSize);
        
        // 最大线程数
        executor.setMaxPoolSize(maxPoolSize);
        
        // 队列容量
        executor.setQueueCapacity(queueCapacity);
        
        // 线程空闲时间
        executor.setKeepAliveSeconds(keepAliveSeconds);
        
        // 线程名前缀
        executor.setThreadNamePrefix(threadNamePrefix);
        
        // 拒绝策略：调用者运行策略，确保任务不丢失
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 等待所有任务完成后再关闭线程池
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 等待时间
        executor.setAwaitTerminationSeconds(60);
        
        // 初始化
        executor.initialize();
        
        log.info("通知异步线程池配置完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}, threadNamePrefix={}", 
                corePoolSize, maxPoolSize, queueCapacity, threadNamePrefix);
        
        return executor;
    }

    /**
     * 批量处理异步执行器
     * 专门用于批量通知处理
     */
    @Bean(name = "notificationBatchExecutor")
    public Executor notificationBatchExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 批量处理使用更少的线程，但更大的队列
        executor.setCorePoolSize(Math.max(2, corePoolSize / 2));
        executor.setMaxPoolSize(Math.max(5, maxPoolSize / 2));
        executor.setQueueCapacity(queueCapacity * 2);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("NotificationBatch-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        
        log.info("批量通知异步线程池配置完成: corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                executor.getCorePoolSize(), executor.getMaxPoolSize(), queueCapacity * 2);
        
        return executor;
    }
}
