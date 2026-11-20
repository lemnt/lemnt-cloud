package com.lemnt.common.security.config;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

import java.util.TimeZone;

/**
 * 系统配置
 *
 * @author wangxx
 */
public class ApplicationConfig {
    /**
     * 时区配置
     */
    @Bean
    public JsonMapperBuilderCustomizer jacksonObjectMapperCustomization() {
        return jsonMapperBuilder -> jsonMapperBuilder.defaultTimeZone(TimeZone.getDefault());
    }
}
