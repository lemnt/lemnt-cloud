package com.lemnt.common.oss.configure;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.context.ConfigurationPropertiesAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.MapPropertySource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** 验证已有消费者在配置刷新后使用新客户端，不访问真实 OSS。 */
class MinioConfigRefreshTest {
    @Test
    void shouldRefreshClientUsedByExistingConsumer() {
        Map<String, Object> properties = new HashMap<>(Map.of(
                "minio.endpoint", "http://localhost:19000",
                "minio.url", "https://old.example.test",
                "minio.accessKey", "old-test-access",
                "minio.secretKey", "old-test-secret",
                "minio.bucketName", "old-bucket"));

        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        ConfigurationPropertiesAutoConfiguration.class, RefreshAutoConfiguration.class))
                .withUserConfiguration(MinioConfig.class, ConsumerConfig.class)
                .withInitializer(context -> context.getEnvironment().getPropertySources()
                        .addFirst(new MapPropertySource("testMinio", properties)))
                .run(context -> {
                    Consumer consumer = context.getBean(Consumer.class);
                    assertThat(consumer.signedUrl()).contains("localhost:19000/old-bucket/test.png")
                            .contains("Credential=old-test-access");

                    properties.put("minio.endpoint", "http://localhost:19001");
                    properties.put("minio.url", "https://new.example.test");
                    properties.put("minio.accessKey", "new-test-access");
                    properties.put("minio.secretKey", "new-test-secret");
                    properties.put("minio.bucketName", "new-bucket");
                    // 模拟 Nacos 的环境变更通知及 Spring Cloud 刷新作用域失效。
                    context.publishEvent(new EnvironmentChangeEvent(properties.keySet()));
                    context.getBean(RefreshScope.class).refreshAll();

                    assertThat(context.getBean(Consumer.class)).isSameAs(consumer);
                    assertThat(consumer.signedUrl()).contains("localhost:19001/new-bucket/test.png")
                            .contains("Credential=new-test-access");
                    assertThat(consumer.config().getUrl()).isEqualTo("https://new.example.test");
                    assertThat(consumer.config().getSecretKey()).isEqualTo("new-test-secret");
                });
    }

    @Configuration(proxyBeanMethods = false)
    static class ConsumerConfig {
        @Bean
        Consumer consumer(MinioConfig config, MinioClient client) {
            return new Consumer(config, client);
        }
    }

    record Consumer(MinioConfig config, MinioClient client) {
        String signedUrl() throws Exception {
            // 显式指定 region，签名只在本地生成，不查询存储服务。
            return client.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Method.GET).region("us-east-1").bucket(config.getBucketName())
                    .object("test.png").build());
        }
    }
}
