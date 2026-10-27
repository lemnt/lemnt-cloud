package com.lemnt.mall.start;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lemnt商城应用启动。
 */
@EnableDubbo(scanBasePackages = "com.lemnt.mall")
@MapperScan(basePackages = "com.lemnt.mall.infrastructure", annotationClass = Mapper.class)
@SpringBootApplication(scanBasePackages = "com.lemnt.mall")
public class LemntMallApplication {

    /**
     * 主。
     */
    public static void main(String[] args) {
        SpringApplication.run(LemntMallApplication.class, args);
    }
}
