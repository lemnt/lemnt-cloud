package com.lemnt.order.start;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Lemnt交易应用启动。
 */
@EnableScheduling
@EnableDubbo(scanBasePackages = "com.lemnt.order")
@MapperScan(basePackages = "com.lemnt.order.infrastructure", annotationClass = Mapper.class)
@SpringBootApplication(scanBasePackages = "com.lemnt.order")
public class LemntOrderApplication {

    /**
     * 主。
     */
    public static void main(String[] args) {
        SpringApplication.run(LemntOrderApplication.class, args);
    }
}
