package com.example.async.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 * 
 * 配置两个线程池：
 * 1. taskExecutor: 任务执行线程池，用于异步执行业务任务
 * 2. schedulerExecutor: 调度器线程池，用于定时拉取和调度任务
 * 
 * @author AsyncTaskFramework
 */
@Slf4j
@Configuration
@EnableAsync
public class ThreadPoolConfig {
    
    @Value("${async-task.thread-pool.core-pool-size:8}")
    private int corePoolSize;
    
    @Value("${async-task.thread-pool.max-pool-size:16}")
    private int maxPoolSize;
    
    @Value("${async-task.thread-pool.queue-capacity:1000}")
    private int queueCapacity;
    
    @Value("${async-task.thread-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;
    
    @Value("${async-task.thread-pool.allow-core-thread-timeout:true}")
    private boolean allowCoreThreadTimeOut;
    
    @Value("${async-task.thread-pool.thread-name-prefix:async-task-}")
    private String threadNamePrefix;
    
    /**
     * 任务执行线程池
     * 
     * 核心配置：
     * - 核心线程数：8（默认为CPU核心数）
     * - 最大线程数：16（默认为CPU核心数*2）
     * - 队列容量：1000
     * - 拒绝策略：CallerRunsPolicy（由调用线程执行，保证任务不丢失）
     * - 优雅关闭：等待60秒让任务完成
     * 
     * @return 配置好的线程池执行器
     */
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        log.info("Initializing task executor with corePoolSize={}, maxPoolSize={}, queueCapacity={}", 
                 corePoolSize, maxPoolSize, queueCapacity);

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 核心线程池大小
        executor.setCorePoolSize(corePoolSize);
        
        // 最大线程池大小
        executor.setMaxPoolSize(maxPoolSize);
        
        // 队列容量
        executor.setQueueCapacity(queueCapacity);
        
        // 线程名称前缀
        executor.setThreadNamePrefix(threadNamePrefix);
        
        // 空闲线程存活时间（秒）
        executor.setKeepAliveSeconds(keepAliveSeconds);
        
        // 允许核心线程超时
        executor.setAllowCoreThreadTimeOut(allowCoreThreadTimeOut);
        
        // 拒绝策略：当队列满时，由调用线程执行该任务
        // 这样可以保证任务不会丢失，但会降低提交速度，起到背压作用
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 优雅关闭：等待所有任务完成后再关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        
        // 优雅关闭等待时间（秒）
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("Task executor initialized successfully");
        return executor;
    }
    
    /**
     * 调度器线程池
     * 
     * 用于执行定时任务，如从Redis拉取到期任务
     * 
     * 核心配置：
     * - 核心线程数：2
     * - 最大线程数：4
     * - 队列容量：100
     * - 拒绝策略：CallerRunsPolicy
     * - 优雅关闭：等待30秒
     * 
     * @return 配置好的调度器线程池
     */
    @Bean(name = "schedulerExecutor")
    public Executor schedulerExecutor() {
        log.info("Initializing scheduler executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // 调度器线程池可以小一些，主要用于定时任务
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-scheduler-");
        executor.setKeepAliveSeconds(60);
        executor.setAllowCoreThreadTimeOut(true);
        
        // 拒绝策略
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // 优雅关闭
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        log.info("Scheduler executor initialized successfully");
        return executor;
    }
}