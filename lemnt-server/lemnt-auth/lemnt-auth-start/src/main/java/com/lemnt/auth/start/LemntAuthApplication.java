package com.lemnt.auth.start;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 认证授权中心
 *
 * @author wangxx
 */
@EnableDubbo(scanBasePackages = "com.lemnt.auth")
@SpringBootApplication(scanBasePackages = "com.lemnt.auth")
public class LemntAuthApplication {

    public static void main(String[] args) {
        SpringApplication.run(LemntAuthApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  认证授权中心启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}
