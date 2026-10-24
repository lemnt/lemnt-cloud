package com.lemnt.open.start;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 开放平台模块
 *
 * @author wangxx
 */
@EnableAsync
@EnableDubbo
@SpringBootApplication
public class LemntOpenApplication {

    public static void main(String[] args) {
        SpringApplication.run(LemntOpenApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  开放平台模块启动成功   ლ(´ڡ`ლ)ﾞ ");
    }
}
