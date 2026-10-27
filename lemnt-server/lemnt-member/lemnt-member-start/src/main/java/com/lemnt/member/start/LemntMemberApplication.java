package com.lemnt.member.start;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Lemnt会员应用启动。
 */
@EnableDubbo(scanBasePackages = "com.lemnt.member")
@MapperScan(basePackages = "com.lemnt.member.infrastructure", annotationClass = Mapper.class)
@SpringBootApplication(scanBasePackages = "com.lemnt.member")
public class LemntMemberApplication {

    /**
     * 主。
     */
    public static void main(String[] args) {
        SpringApplication.run(LemntMemberApplication.class, args);
    }
}
