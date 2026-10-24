package com.lemnt.common.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lemnt.kafka")
public class CustomKafkaProperties {

    private boolean enabled = true;
}
