package com.lemnt.common.security.config;

import java.util.TimeZone;

import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

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
