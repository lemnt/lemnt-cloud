package com.lemnt.common.kafka.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "lemnt.kafka")
public class CustomKafkaProperties {

    private boolean enabled = true;
}
