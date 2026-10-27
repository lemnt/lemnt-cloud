package com.lemnt.mall.infrastructure.checkout;

import com.lemnt.common.redis.serializer.Fastjson2RedisSerializer;
import com.lemnt.mall.domain.checkout.model.CartReference;
import com.lemnt.mall.domain.checkout.model.CheckoutQuote;
import com.lemnt.mall.domain.checkout.model.Settlement;
import com.lemnt.mall.domain.checkout.model.CheckoutItem;
import com.lemnt.mall.infrastructure.checkout.gateway.SettlementGatewayImpl;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SettlementCacheTest {
    Settlement snapshot() {
        return new Settlement(
                "12345678-1234-1234-1234-123456789abc",
                1L,
                new CheckoutQuote(
                List.of(
                new CheckoutItem(
                5L,
                "SKU",
                "name",
                new BigDecimal("10.00"),
                2,
                new BigDecimal("20.00"))),
                new BigDecimal("20.00"),
                new BigDecimal("0.00"),
                new BigDecimal("20.00"),
                null),
                List.of(new CartReference(3L, 0)),
                Instant.now().plusSeconds(900));
    }

    @Test
    void actualSerializerRoundTripsImmutableQuoteAndCartReferences() {
        var codec = new Fastjson2RedisSerializer();
        var value = snapshot();
        var result = codec.deserialize(codec.serialize(value), Settlement.class);
        assertEquals(value.id(), result.id());
        assertEquals(value.quote(), result.quote());
        assertEquals(value.references(), result.references());
        assertEquals(value.expiresAt(), result.expiresAt());
    }

    @Test
    void redisWriteRequiresNxSuccessAndBoundedTtl() {
        var redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        var gateway = new SettlementGatewayImpl(redis, new Fastjson2RedisSerializer());
        var value = snapshot();
        when(values.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        gateway.cache(value);
        var ttl = ArgumentCaptor.forClass(Duration.class);
        verify(values)
            .setIfAbsent(eq("mall:settlement:v1:" + value.id()), anyString(), ttl.capture());
        assertTrue(ttl.getValue().getSeconds() > 890 && ttl.getValue().getSeconds() <= 900);
        when(values.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> gateway.cache(value));
    }

    @Test
    void cacheMissIsExplicitAndDoesNotBuildFallbackSnapshot() {
        var redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        var gateway =
        new SettlementGatewayImpl(
                redis, new Fastjson2RedisSerializer());
        assertNull(gateway.cached(snapshot().id()));
    }
}
