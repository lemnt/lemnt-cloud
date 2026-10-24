package com.lemnt.common.start.config;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * 初始化日志路径
 *
 * @author wangxx
 * @version 1.0.0
 * @date 2025-12-01 14:52
 */
public class ApplicationLoggerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        ConfigurableEnvironment environment = applicationContext.getEnvironment();
        String appName = environment.getProperty("spring.application.name");
        String logBase = environment.getProperty("LOGGING_PATH", "logs");
        System.setProperty("logging.file.name", String.format("%s/%s/debug.log", logBase, appName));
    }

}
