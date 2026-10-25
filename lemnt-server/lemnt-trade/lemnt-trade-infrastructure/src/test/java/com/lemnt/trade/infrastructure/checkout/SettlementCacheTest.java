package com.lemnt.trade.infrastructure.checkout;

import com.lemnt.trade.infrastructure.checkout.gateway.SettlementGatewayImpl;
import com.lemnt.trade.infrastructure.checkout.mapper.SettlementMapper;
import com.lemnt.trade.domain.checkout.model.*;
import com.lemnt.trade.domain.model.TradeOrderItem;
import com.lemnt.common.redis.serializer.Fastjson2RedisSerializer;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.List;
import java.math.BigDecimal;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class SettlementCacheTest {
    Settlement snapshot() {
        return new Settlement(
                "12345678-1234-1234-1234-123456789abc",
                1L,
                new CheckoutQuote(
                        List.of(
                                new TradeOrderItem(
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
        var mapper = mock(SettlementMapper.class);
        var gateway = new SettlementGatewayImpl(redis, new Fastjson2RedisSerializer(), mapper);
        var value = snapshot();
        when(values.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        gateway.cache(value);
        var ttl = org.mockito.ArgumentCaptor.forClass(Duration.class);
        verify(values)
                .setIfAbsent(eq("trade:settlement:v1:" + value.id()), anyString(), ttl.capture());
        assertTrue(ttl.getValue().getSeconds() > 890 && ttl.getValue().getSeconds() <= 900);
        when(values.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> gateway.cache(value));
        verifyNoInteractions(mapper);
    }

    @Test
    void cacheMissIsExplicitAndDoesNotBuildFallbackSnapshot() {
        var redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        var gateway =
                new SettlementGatewayImpl(
                        redis, new Fastjson2RedisSerializer(), mock(SettlementMapper.class));
        assertNull(gateway.cached(snapshot().id()));
    }
}
