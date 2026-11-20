package com.lemnt.pay.start.payment;

import com.lemnt.pay.domain.channel.gateway.PayAppGateway;
import com.lemnt.pay.infrastructure.channel.support.PayChannelConfigResolver;

import com.baomidou.lock.LockTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Fallback;

import static org.mockito.Mockito.mock;

/** 支付库 Mapper 测试不连接 Redis，为跨服务锁和渠道配置提供测试依赖。 */
@TestConfiguration(proxyBeanMethods = false)
class PaymentPersistenceDependencies {
    @Bean
    PayChannelConfigResolver channelConfigs() {
        return mock(PayChannelConfigResolver.class);
    }

    @Bean
    @Fallback
    PayAppGateway payApps() {
        return mock(PayAppGateway.class);
    }

    @Bean
    LockTemplate lockTemplate() {
        return mock(LockTemplate.class);
    }
}
