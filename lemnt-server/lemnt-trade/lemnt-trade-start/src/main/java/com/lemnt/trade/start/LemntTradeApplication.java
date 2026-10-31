package com.lemnt.trade.start;

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
@EnableDubbo(scanBasePackages = "com.lemnt.trade")
@MapperScan(basePackages = "com.lemnt.trade.infrastructure", annotationClass = Mapper.class)
@SpringBootApplication(scanBasePackages = "com.lemnt.trade")
public class LemntTradeApplication {

    /**
     * 主。
     */
    public static void main(String[] args) {
        SpringApplication.run(LemntTradeApplication.class, args);
    }
}
