package com.lemnt.mall.infrastructure.checkout.gateway;

import com.lemnt.common.redis.serializer.RedisSerializer;
import com.lemnt.mall.domain.checkout.gateway.SettlementGateway;
import com.lemnt.mall.domain.checkout.model.Settlement;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.Duration;
import java.time.Instant;

/**
 * 结算领域网关实现。
 */
@Component
@RequiredArgsConstructor
public class SettlementGatewayImpl implements SettlementGateway {
    private final StringRedisTemplate redis;
    private final RedisSerializer serializer;
    private String key(String id) {
        return "mall:settlement:v1:" + id;
    }

    /**
     * 缓存。
     */
    public void cache(Settlement value) {
        var ttl = Duration.between(Instant.now(), value.expiresAt());
        if (ttl.isNegative()
                || ttl.isZero()
                || !Boolean.TRUE.equals(
                redis.opsForValue()
                .setIfAbsent(key(value.id()), serializer.serialize(value), ttl))) {
            throw new IllegalStateException("结算单缓存写入失败");
        }
    }

    /**
     * 已缓存。
     */
    public Settlement cached(String id) {
        var json = redis.opsForValue().get(key(id));
        return json == null ? null : serializer.deserialize(json, Settlement.class);
    }

    /** 已确认提交快照保留至订单恢复窗口结束，不覆盖原始内容。 */
    @Override
    public void cacheOrder(com.lemnt.mall.domain.order.model.OrderSnapshot value) {
        redis.opsForValue().setIfAbsent("mall:checkout:submission:" + value.orderNo(),
                serializer.serialize(value), Duration.ofDays(30));
    }

    @Override
    public com.lemnt.mall.domain.order.model.OrderSnapshot cachedOrder(String orderNo) {
        var json = redis.opsForValue().get("mall:checkout:submission:" + orderNo);
        return json == null ? null : serializer.deserialize(json,
                com.lemnt.mall.domain.order.model.OrderSnapshot.class);
    }
}
