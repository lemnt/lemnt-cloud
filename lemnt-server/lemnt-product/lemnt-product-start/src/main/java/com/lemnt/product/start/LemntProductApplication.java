package com.lemnt.product.start;

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
@EnableDubbo(scanBasePackages = "com.lemnt.product")
@SpringBootApplication(scanBasePackages = "com.lemnt.product")
@MapperScan(basePackages = "com.lemnt.product.infrastructure.*.mapper")
public class LemntProductApplication {

    /**
     * 主。
     */
    public static void main(String[] args) {
        SpringApplication.run(LemntProductApplication.class, args);
    }
}
