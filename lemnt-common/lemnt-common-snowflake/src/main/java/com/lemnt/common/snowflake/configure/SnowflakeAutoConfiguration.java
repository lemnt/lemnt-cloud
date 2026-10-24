package com.lemnt.common.snowflake.configure;

import cn.hutool.core.lang.Snowflake;
import com.lemnt.common.snowflake.utils.SnowflakeUtils;
import com.lemnt.common.snowflake.core.SafeSnowflake;
import com.lemnt.common.snowflake.core.WorkerIdAssigner;
import com.lemnt.common.snowflake.properties.SnowflakeProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(SnowflakeProperties.class)
@ConditionalOnProperty(prefix = "snowflake", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnClass(name = "cn.hutool.core.lang.Snowflake")
public class SnowflakeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public WorkerIdAssigner workerIdAssigner(StringRedisTemplate stringRedisTemplate, SnowflakeProperties properties,
                                             @Value("${spring.application.name:default-service}") String serviceName) {
        return new WorkerIdAssigner(stringRedisTemplate, properties, serviceName);
    }

    @Bean
    @ConditionalOnMissingBean
    public Snowflake snowflake(WorkerIdAssigner assigner, SnowflakeProperties properties) {
        return new SafeSnowflake(assigner.getWorkerId(), properties.getDatacenterId());
    }

    @Bean
    @ConditionalOnMissingBean
    public SnowflakeUtils idGenerator(Snowflake snowflake) {
        return new SnowflakeUtils(snowflake);
    }
}
