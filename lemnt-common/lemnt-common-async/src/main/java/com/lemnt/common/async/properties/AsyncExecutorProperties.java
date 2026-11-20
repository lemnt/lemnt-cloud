package com.lemnt.common.async.properties;

import com.lemnt.common.async.model.ExecutorConfig;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 异步线程配置
 *
 * @author wangxx
 */
@Data
@ConfigurationProperties(prefix = "async.executor")
public class AsyncExecutorProperties {

    private ExecutorConfig task = new ExecutorConfig();
}
