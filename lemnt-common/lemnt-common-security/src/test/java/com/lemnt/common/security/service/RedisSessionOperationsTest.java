package com.lemnt.common.security.service;

import com.lemnt.common.redis.service.RedisStringService;
import com.lemnt.common.redis.serializer.RedisSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/** 验证共享Redis封装调用游标和原子SET XX，不连接真实Redis。 */
class RedisSessionOperationsTest {
    @Test
    @SuppressWarnings("unchecked")
    void scanClosesCursorWhenConsumerFailsWithoutUsingKeys() {
        var template = mock(StringRedisTemplate.class, withSettings().mockMaker("mock-maker-subclass"));
        Cursor<String> cursor = mock(Cursor.class, withSettings().mockMaker("mock-maker-subclass"));
        when(template.scan(any(ScanOptions.class))).thenReturn(cursor);
        doAnswer(invocation -> {
            Consumer<String> action = invocation.getArgument(0);
            action.accept("login_tokens:session");
            return null;
        }).when(cursor).forEachRemaining(any());
        var service = new RedisStringService();
        ReflectionTestUtils.setField(service, "redisTemplate", template);
        assertThrows(IllegalStateException.class, () -> service.scan("login_tokens:*", key -> {
            throw new IllegalStateException("缓存读取失败");
        }));
        verify(cursor).close();
        verify(template, never()).keys(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void setIfPresentPreservesSerializationAndTtl() {
        var template = mock(StringRedisTemplate.class, withSettings().mockMaker("mock-maker-subclass"));
        ValueOperations<String, String> values = mock(ValueOperations.class, withSettings().mockMaker("mock-maker-subclass"));
        var serializer = mock(RedisSerializer.class, withSettings().mockMaker("mock-maker-subclass"));
        when(template.opsForValue()).thenReturn(values);
        when(serializer.serialize("value")).thenReturn("serialized");
        when(values.setIfPresent("session", "serialized", 30L, TimeUnit.DAYS)).thenReturn(true, false);
        var service = new RedisStringService();
        ReflectionTestUtils.setField(service, "redisTemplate", template);
        ReflectionTestUtils.setField(service, "redisSerializer", serializer);
        assertTrue(service.setObjIfPresent("session", "value", 30L, TimeUnit.DAYS));
        assertFalse(service.setObjIfPresent("session", "value", 30L, TimeUnit.DAYS));
        verify(values, times(2)).setIfPresent("session", "serialized", 30L, TimeUnit.DAYS);
    }
}
