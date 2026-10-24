package com.lemnt.common.lock.config;

import com.baomidou.lock.spring.boot.autoconfigure.LockAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * RedissonConfig配置
 *
 * @author wangxx
 * @version 1.0.0
 * @date 2025/11/9 16:48
 */
@Slf4j
@Configuration
@AutoConfigureBefore(LockAutoConfiguration.class)
public class RedissonConfig {

    @Value("${spring.redis.cluster.nodes:}")
    private List<String> clusterNodes;

    @Value("${spring.redis.cluster.max-redirects:3}")
    private Integer maxRedirects;

    @Value("${spring.redis.host:127.0.0.1}")
    private String host;

    @Value("${spring.redis.port:6379}")
    private Integer port;

    @Value("${spring.redis.database:0}")
    private Integer database;

    @Value("${spring.redis.username:default}")
    private String username;

    @Value("${spring.redis.password:}")
    private String password;

    @ConditionalOnMissingBean
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        if (clusterNodes != null && !clusterNodes.isEmpty()) {
            // 集群模式
            String[] nodes = clusterNodes.stream()
                    .map(String::trim)
                    .map(n -> n.startsWith("redis://") ? n : "redis://" + n)
                    .toArray(String[]::new);

            config.useClusterServers()
                    .addNodeAddress(nodes)
                    .setUsername(username)
                    .setPassword(password.isEmpty() ? null : password)
                    .setScanInterval(2000)
                    .setMasterConnectionPoolSize(16)
                    .setMasterConnectionMinimumIdleSize(8)
                    .setSlaveConnectionPoolSize(8)
                    .setSlaveConnectionMinimumIdleSize(8)
                    .setIdleConnectionTimeout(10000)
                    .setConnectTimeout(10000)
                    .setTimeout(3000);

            log.info("[RedissonConfig] RedissonClient initialized in cluster mode: {}", String.join(",", nodes));
        } else {
            // 单节点模式
            String address = "redis://" + host + ":" + port;
            config.useSingleServer()
                    .setAddress(address)
                    .setPassword(password.isEmpty() ? null : password)
                    .setUsername(username)
                    .setDatabase(database)
                    .setConnectionPoolSize(16)
                    .setConnectionMinimumIdleSize(8)
                    .setIdleConnectionTimeout(10000)
                    .setConnectTimeout(10000)
                    .setTimeout(3000);

            log.info("[RedissonConfig] RedissonClient initialized in single server mode: {}", address);
        }
        return Redisson.create(config);
    }
}
