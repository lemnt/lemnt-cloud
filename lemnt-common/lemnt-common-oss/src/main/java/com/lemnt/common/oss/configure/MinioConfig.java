package com.lemnt.common.oss.configure;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Minio 配置信息
 *
 * @author wangxx
 */
@Configuration
@ConfigurationProperties(prefix = "minio")
@RefreshScope
public class MinioConfig {
    /**
     * 服务地址
     */
    private String url;

    /**
     * endpoint
     */
    private String endpoint;

    /**
     * 用户名
     */
    private String accessKey;

    /**
     * 密码
     */
    private String secretKey;

    /**
     * 存储桶名称
     */
    private String bucketName;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    @Bean
    @RefreshScope
    public MinioClient getMinioClient() {
        // Nacos 配置刷新后重建客户端，已有消费者通过刷新代理获取新地址和凭据。
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }
}
