package com.lemnt.common.job.config;

import com.lemnt.common.job.model.XxlJobProperties;
import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * XxlJob 自动注册
 *
 * @author wangxx
 * @version 1.0.0
 * @date 2025/7/26 22:02
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(XxlJobProperties.class)
@ConditionalOnProperty(prefix = "xxl.job.executor", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CustomXxlJobConfig {

    private final XxlJobProperties xxlJobProperties;

    public CustomXxlJobConfig(XxlJobProperties xxlJobProperties) {
        this.xxlJobProperties = xxlJobProperties;
    }

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        XxlJobSpringExecutor executor = new XxlJobSpringExecutor();
        // Admin 配置
        executor.setAdminAddresses(xxlJobProperties.getAdmin().getAddresses());
        executor.setAccessToken(xxlJobProperties.getAdmin().getAccessToken());
        // Executor 配置
        executor.setAppname(xxlJobProperties.getExecutor().getAppname());
        executor.setAddress(xxlJobProperties.getExecutor().getAddress());
        executor.setIp(xxlJobProperties.getExecutor().getIp());
        executor.setPort(xxlJobProperties.getExecutor().getPort());
        executor.setLogPath(xxlJobProperties.getExecutor().getLogpath());
        executor.setLogRetentionDays(xxlJobProperties.getExecutor().getLogretentiondays());
        return executor;
    }
}
