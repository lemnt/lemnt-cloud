package com.lemnt.common.async.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class AsyncConfigTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withUserConfiguration(ProbeConfiguration.class)
            .withPropertyValues("async.executor.task.core-pool-size=1",
                    "async.executor.task.max-pool-size=1",
                    "async.executor.task.thread-name-prefix=audit-regression-");

    @Test
    void shouldRegisterOnlyOneAsyncConfigurerWithBootTaskConfiguration() {
        contextRunner.run(context -> assertThat(context).hasSingleBean(AsyncConfigurer.class));
    }

    @Test
    void shouldRunAsyncMethodOnConfiguredSingletonExecutor() {
        contextRunner.run(context -> {
            // 实际调用异步代理，复现审计在选择默认执行器时抛出的异常。
            String threadName = context.getBean(AsyncProbe.class).threadName().get(5, TimeUnit.SECONDS);
            assertThat(threadName).startsWith("audit-regression-");
            assertThat(context.getBean(AsyncConfigurer.class).getAsyncExecutor())
                    .isSameAs(context.getBean("asyncTaskExecutor"));
        });
    }

    @Configuration(proxyBeanMethods = false)
    @EnableAsync
    @EnableAutoConfiguration
    static class ProbeConfiguration {

        @Bean
        AsyncProbe asyncProbe() {
            return new AsyncProbe();
        }
    }

    static class AsyncProbe {

        /**
         * 返回实际执行线程，验证无名称的 @Async 使用项目配置的线程池。
         */
        @Async
        public CompletableFuture<String> threadName() {
            return CompletableFuture.completedFuture(Thread.currentThread().getName());
        }
    }
}
