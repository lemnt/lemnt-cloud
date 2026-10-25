package com.lemnt.member.start;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableDubbo(scanBasePackages = "com.lemnt.member")
@MapperScan(basePackages = "com.lemnt.member.infrastructure", annotationClass = Mapper.class)
@SpringBootApplication(scanBasePackages = "com.lemnt.member")
public class LemntMemberApplication {
    public static void main(String[] args) {
        SpringApplication.run(LemntMemberApplication.class, args);
    }
}
