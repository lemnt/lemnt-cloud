package com.lemnt.common.async.config;

import com.lemnt.common.async.model.ExecutorConfig;
import com.lemnt.common.async.properties.AsyncExecutorProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 异步线程池配置
 * 在 Boot 默认任务配置之前注册，避免 Web 配置提前引入第二个 AsyncConfigurer。
 *
 * @author wangxx
 */
@Slf4j
@EnableAsync
@Configuration
@AutoConfigureBefore(TaskExecutionAutoConfiguration.class)
@EnableConfigurationProperties(AsyncExecutorProperties.class)
public class AsyncConfig implements AsyncConfigurer {

    private final AsyncExecutorProperties properties;

    public AsyncConfig(AsyncExecutorProperties properties) {
        this.properties = properties;
    }

    /**
     * 异步线程池
     */
    @Bean(name = "asyncTaskExecutor")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        return buildExecutor(properties.getTask());
    }

    /**
     * 指定 @Async 默认线程池
     * <p>
     * 通过 @Configuration 代理机制复用 asyncTaskExecutor Bean，避免重复创建。
     * </p>
     */
    @Override
    public Executor getAsyncExecutor() {
        return asyncTaskExecutor();
    }

    /**
     * 处理 @Async void 方法的未捕获异常
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) -> log.error(
                "Async error. method={}, params={}",
                method.getName(), params, ex
        );
    }

    /**
     * 根据配置构建线程池
     *
     * @param config 线程池配置参数
     * @return 初始化完成的线程池
     */
    private ThreadPoolTaskExecutor buildExecutor(ExecutorConfig config) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(config.getCorePoolSize());
        executor.setMaxPoolSize(config.getMaxPoolSize());
        executor.setQueueCapacity(config.getQueueCapacity());
        executor.setThreadNamePrefix(config.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setAllowCoreThreadTimeOut(true);
        executor.setTaskDecorator(new MdcTaskDecorator());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        executor.getThreadPoolExecutor().prestartAllCoreThreads();
        return executor;
    }
}
