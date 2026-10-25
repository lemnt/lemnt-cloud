package com.lemnt.trade.start;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = "com.lemnt.trade")
public class LemntTradeApplication {
    public static void main(String[] args) {
        SpringApplication.run(LemntTradeApplication.class, args);
    }
}
