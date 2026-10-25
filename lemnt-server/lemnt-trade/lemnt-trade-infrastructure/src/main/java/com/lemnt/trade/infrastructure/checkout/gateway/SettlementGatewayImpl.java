package com.lemnt.trade.infrastructure.checkout.gateway;

import com.lemnt.trade.domain.checkout.gateway.SettlementGateway;
import com.lemnt.trade.domain.checkout.model.Settlement;
import com.lemnt.trade.infrastructure.checkout.mapper.SettlementMapper;
import com.lemnt.common.redis.serializer.RedisSerializer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.time.Duration;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class SettlementGatewayImpl implements SettlementGateway {
    private final StringRedisTemplate redis;
    private final RedisSerializer serializer;
    private final SettlementMapper mapper;

    private String key(String id) {
        return "trade:settlement:v1:" + id;
    }

    public void cache(Settlement value) {
        var ttl = Duration.between(Instant.now(), value.expiresAt());
        if (ttl.isNegative()
                || ttl.isZero()
                || !Boolean.TRUE.equals(
                        redis.opsForValue()
                                .setIfAbsent(key(value.id()), serializer.serialize(value), ttl)))
            throw new IllegalStateException("结算单缓存写入失败");
    }

    public Settlement cached(String id) {
        var json = redis.opsForValue().get(key(id));
        return json == null ? null : serializer.deserialize(json, Settlement.class);
    }

    public String orderNo(Long memberId, String id) {
        return mapper.orderNo(memberId, id);
    }

    public void bind(Long memberId, String id, String orderNo) {
        mapper.bind(memberId, id, orderNo);
    }
}
