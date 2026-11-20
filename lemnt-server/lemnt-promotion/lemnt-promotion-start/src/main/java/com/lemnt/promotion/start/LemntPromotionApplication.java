package com.lemnt.promotion.start;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Lemnt商城应用启动。
 */
@EnableScheduling
@EnableDubbo(scanBasePackages = "com.lemnt.promotion")
@MapperScan(basePackages = "com.lemnt.promotion.infrastructure", annotationClass = Mapper.class)
@SpringBootApplication(scanBasePackages = "com.lemnt.promotion")
public class LemntPromotionApplication {

    /**
     * 主。
     */
    public static void main(String[] args) {
        SpringApplication.run(LemntPromotionApplication.class, args);
    }
}
