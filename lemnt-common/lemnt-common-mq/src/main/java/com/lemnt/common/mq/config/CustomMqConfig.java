package com.lemnt.common.mq.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Mq 配置
 *
 * @author wangxx
 * @version 1.0.0
 * @date 2025/7/26 22:02
 */
@Configuration
public class CustomMqConfig {

    @Value("${rocketmq.name-server}")
    private String nameServer;

    @Value("${rocketmq.producer.group}")
    private String producerGroup;
}
