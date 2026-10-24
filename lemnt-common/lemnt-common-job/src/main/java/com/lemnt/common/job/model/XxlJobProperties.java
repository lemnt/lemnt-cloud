package com.lemnt.common.job.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * xxl job 配置项
 *
 * @author wangxx
 * @version 1.0.0
 * @date 2025/7/26 22:02
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "xxl.job")
public class XxlJobProperties {

    private Admin admin;

    private Executor executor;

    @Data
    public static class Admin {
        private String addresses;
        private String accessToken;
    }

    @Data
    public static class Executor {
        private Boolean enabled;
        private String appname;
        private String address;
        private String ip;
        private int port;
        private String logpath;
        private int logretentiondays;
    }
}
