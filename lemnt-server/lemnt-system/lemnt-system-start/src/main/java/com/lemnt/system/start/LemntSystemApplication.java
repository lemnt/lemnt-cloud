package com.lemnt.system.start;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 系统模块
 *
 * @author wangxx
 */
@EnableAsync
@EnableDubbo(scanBasePackages = "com.lemnt.system")
@SpringBootApplication(scanBasePackages = "com.lemnt.system")
@MapperScan(basePackages = "com.lemnt.system.infrastructure.*.mapper")
public class LemntSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(LemntSystemApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  系统模块启动成功   ლ(´ڡ`ლ)ﾞ  \n");
    }
}
