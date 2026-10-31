package com.lemnt.mall.infrastructure.settlement;

import com.lemnt.common.redis.serializer.Fastjson2RedisSerializer;
import com.lemnt.mall.domain.cart.model.CartReference;
import com.lemnt.mall.domain.settlement.model.Settlement;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementQuote;
import com.lemnt.mall.infrastructure.settlement.gateway.SettlementGatewayImpl;

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
                new SettlementQuote(
                List.of(
                new SettlementItem(
                5L,
                "SKU",
                "name",
                new BigDecimal("10.00"),
                2,
                new BigDecimal("20.00"), new BigDecimal("20.00"), BigDecimal.ZERO,
                8L, "原商品", List.of(new SettlementItem.Property(3L, "颜色", 7L, "黑色")), "https://example.com/original.png")),
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
    void freightAndRecipientRoundTripWithProductionRedisCodec() {
        var codec = new Fastjson2RedisSerializer();
        var original = snapshot();
        var quote = new SettlementQuote(List.of(new SettlementItem(5L, "SKU", "name", new BigDecimal("10.00"),
                2, new BigDecimal("20.00"), new BigDecimal("26.50"), new BigDecimal("6.50"))),
                new BigDecimal("20.00"), new BigDecimal("0.00"), new BigDecimal("26.50"), null, 0L, 53L,
                new com.lemnt.mall.domain.settlement.model.SettlementDelivery(7L, 1, "收件人", "13800000000",
                        110101L, "地址", new BigDecimal("6.50")));
        var value = new Settlement(original.id(), original.memberId(), quote, original.references(), original.expiresAt());
        assertEquals(value, codec.deserialize(codec.serialize(value), Settlement.class));
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
            .setIfAbsent(eq("mall:settlement:v4:" + value.id()), anyString(), ttl.capture());
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

    /** 回滚只能撤销本次新建的缓存，不能删除已经提交的幂等绑定。 */
    @Test
    void rollbackDoesNotRemovePreviouslyCommittedBinding() {
        for (long result : new long[] {1L, 2L}) {
            var redis = mock(StringRedisTemplate.class);
            when(redis.execute(any(org.springframework.data.redis.core.script.RedisScript.class),
                    anyList(), any(Object[].class))).thenReturn(result);
            var gateway = new SettlementGatewayImpl(redis, new Fastjson2RedisSerializer());
            org.springframework.transaction.support.TransactionSynchronizationManager.initSynchronization();
            try {
                gateway.bind(1L, "settlement", "ORDER");
                var callbacks = org.springframework.transaction.support.TransactionSynchronizationManager
                        .getSynchronizations();
                assertEquals(result == 1L ? 1 : 0, callbacks.size());
                callbacks.forEach(c -> c.afterCompletion(
                        org.springframework.transaction.support.TransactionSynchronization.STATUS_ROLLED_BACK));
                verify(redis, times(result == 1L ? 2 : 1)).execute(
                        any(org.springframework.data.redis.core.script.RedisScript.class), anyList(), any(Object[].class));
            } finally {
                org.springframework.transaction.support.TransactionSynchronizationManager.clearSynchronization();
            }
        }
    }

    /** 恢复上下文使用真实序列化器，金额、购物车版本及订单商品不能丢失。 */
    @Test
    void actualSerializerRoundTripsSubmission() {
        var value = snapshot();
        var quote = value.quote();
        var order = new com.lemnt.mall.domain.settlement.model.SettlementOrder(
                null, "ORDER", 1L, quote.payableAmount(), "PENDING", quote.items(), null);
        var record = new com.lemnt.mall.domain.settlement.model.SettlementRecord(
                "ORDER", 1L, "REQUEST", "HASH", quote.originalAmount(), quote.discountAmount(),
                quote.payableAmount(), null, value.references());
        var submission = new com.lemnt.mall.domain.settlement.model.SettlementSubmission(order, record);
        var codec = new Fastjson2RedisSerializer();
        assertEquals(submission, codec.deserialize(codec.serialize(submission),
                com.lemnt.mall.domain.settlement.model.SettlementSubmission.class));
    }
    /** 规格映射顺序不影响令牌，但规格内容、图片和未知状态影响令牌。 */
    @Test
    void tokenIncludesStableAndUnambiguousProductSnapshot() {
        var first = new java.util.LinkedHashMap<String, String>();
        first.put("颜色", "黑色"); first.put("尺码", "L");
        var reverse = new java.util.LinkedHashMap<String, String>();
        reverse.put("尺码", "L"); reverse.put("颜色", "黑色");
        assertEquals(token(first, "old.png"), token(reverse, "old.png"));
        assertNotEquals(token(first, "old.png"), token(first, "new.png"));
        assertNotEquals(token(null, "old.png"), token(java.util.Map.of(), "old.png"));
        assertNotEquals(token(java.util.Map.of("a", "b:c"), "old.png"),
                token(java.util.Map.of("a:b", "c"), "old.png"));
    }

    private String token(java.util.Map<String, String> properties, String image) {
        var item = new SettlementItem(5L, "SKU", "name", BigDecimal.TEN, 1, BigDecimal.TEN,
                BigDecimal.TEN, BigDecimal.ZERO, 8L, "SPU", properties == null ? null : properties.entrySet().stream()
                        .map(entry -> new SettlementItem.Property(null, entry.getKey(), null, entry.getValue())).toList(), image);
        return com.lemnt.mall.domain.settlement.model.SettlementRules.quoteToken(
                new SettlementQuote(List.of(item), BigDecimal.TEN, BigDecimal.ZERO, BigDecimal.TEN, null));
    }


    @Test
    void oldBoundSubmissionRestoresNamesAndPreservesOriginalOrderAndRequest() {
        var snapshot = snapshot();
        var order = new com.lemnt.mall.domain.settlement.model.SettlementOrder(null, "OLD-ORDER", 1L,
                snapshot.quote().payableAmount(), "PENDING", snapshot.quote().items(), null);
        var record = new com.lemnt.mall.domain.settlement.model.SettlementRecord("OLD-ORDER", 1L, "OLD-REQUEST",
                "OLD-HASH", snapshot.quote().originalAmount(), snapshot.quote().discountAmount(),
                snapshot.quote().payableAmount(), null, snapshot.references());
        var codec = new Fastjson2RedisSerializer();
        var json = com.alibaba.fastjson2.JSON.parseObject(codec.serialize(
                new com.lemnt.mall.domain.settlement.model.SettlementSubmission(order, record)));
        json.getJSONObject("order").getJSONArray("items").getJSONObject(0)
                .put("properties", com.alibaba.fastjson2.JSON.parseObject("{\"颜色\":\"黑色\"}"));
        var redis = mock(StringRedisTemplate.class);
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        when(values.get("mall:{settlement}:v2:submission:OLD-ORDER")).thenReturn(json.toJSONString());
        var restored = new SettlementGatewayImpl(redis, codec).submission("OLD-ORDER");
        assertEquals("OLD-ORDER", restored.order().orderNo());
        assertEquals("OLD-REQUEST", restored.record().requestNo());
        assertEquals("OLD-HASH", restored.record().fingerprint());
        assertEquals(List.of(new SettlementItem.Property(null, "颜色", null, "黑色")),
                restored.order().items().get(0).properties());
        verify(values, never()).set(anyString(), anyString());
    }
}
