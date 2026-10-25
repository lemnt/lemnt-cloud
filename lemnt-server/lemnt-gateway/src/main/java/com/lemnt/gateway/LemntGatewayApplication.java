package com.lemnt.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关启动程序
 *
 * @author wangxx
 */
// 网关不引入 JDBC，DataSourceAutoConfiguration 不在 classpath 上，
// 故以类名字符串排除（Boot 4 起该类迁至 spring-boot-jdbc 模块）
@org.apache.dubbo.config.spring.context.annotation.EnableDubbo(
        scanBasePackages = "com.lemnt.gateway")
@SpringBootApplication(
        excludeName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
public class LemntGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(LemntGatewayApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  网关模块启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}
