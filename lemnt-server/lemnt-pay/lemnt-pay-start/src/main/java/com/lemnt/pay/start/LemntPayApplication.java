package com.lemnt.pay.start;

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
@EnableDubbo(scanBasePackages = "com.lemnt.pay")
@MapperScan(basePackages = "com.lemnt.pay.infrastructure", annotationClass = Mapper.class)
@SpringBootApplication(scanBasePackages = "com.lemnt.pay")
public class LemntPayApplication {

    /**
     * 主。
     */
    public static void main(String[] args) {
        SpringApplication.run(LemntPayApplication.class, args);
    }
}
