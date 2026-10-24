package com.lemnt.common.kafka.config;

import com.lemnt.common.kafka.producer.BaseKafkaProducer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * 自动配置
 */
@Configuration
@ConditionalOnClass(KafkaTemplate.class)
public class CustomKafkaConfig {

    @Bean
    public BaseKafkaProducer kafkaProducer(KafkaTemplate<String, Object> template) {
        return new BaseKafkaProducer(template);
    }
}
