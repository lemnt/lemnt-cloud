package com.lemnt.common.start.config;

import com.lemnt.common.start.jackson.BigNumberSerializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.databind.ser.std.ToStringSerializer;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * json配置
 *
 * @author wangxx
 */
@Slf4j
@AutoConfiguration(before = JacksonAutoConfiguration.class)
public class JacksonConfig {

    @Bean
    public JsonMapperBuilderCustomizer customizer() {
        return builder -> {
            // 全局配置序列化返回 JSON 处理（Jackson 3 已内置 java.time 支持，无需 JavaTimeModule）
            SimpleModule javaTimeModule = new SimpleModule();
            javaTimeModule.addSerializer(Long.class, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(Long.TYPE, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(BigInteger.class, BigNumberSerializer.INSTANCE);
            javaTimeModule.addSerializer(BigDecimal.class, ToStringSerializer.instance);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(formatter));
            javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(formatter));
            builder.addModules(javaTimeModule);
            builder.defaultTimeZone(TimeZone.getDefault());
            // 全局配置：只过滤 null，不过滤空数组/空字符串/空对象
            builder.changeDefaultPropertyInclusion(inclusion -> inclusion.withValueInclusion(JsonInclude.Include.NON_NULL));
            log.info("初始化 jackson 配置");
        };
    }

}
