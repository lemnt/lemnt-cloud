package com.lemnt.common.redis.service;

import com.lemnt.common.redis.serializer.RedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.lang.reflect.Type;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis 工具类基类
 * <p>
 * 提供公共的序列化/反序列化方法，序列化实现可切换。
 * 默认使用 Fastjson2RedisSerializer，可通过注入自定义 RedisSerializer 替换。
 * </p>
 */
public abstract class AbstractRedisService {

    @Autowired
    protected StringRedisTemplate redisTemplate;

    @Autowired
    protected RedisSerializer redisSerializer;

    /**
     * 字符串转泛型对象
     */
    protected <T> T convert(String value, Class<T> clazz) {
        return redisSerializer.deserialize(value, clazz);
    }

    /**
     * 字符串转泛型对象（支持泛型类型）
     */
    protected <T> T convert(String value, Type type) {
        return redisSerializer.deserialize(value, type);
    }

    /**
     * 对象转字符串（JSON 序列化）
     */
    protected <T> String convertToString(T value) {
        return redisSerializer.serialize(value);
    }

    /**
     * 设置过期时间
     */
    public Boolean expire(String key, long timeout, TimeUnit unit) {
        return redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 删除 key
     */
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 是否存在 key
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 查找匹配的 key
     */
    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }
}
