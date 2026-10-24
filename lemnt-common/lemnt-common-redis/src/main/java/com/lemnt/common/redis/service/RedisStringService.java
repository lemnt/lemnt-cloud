package com.lemnt.common.redis.service;

import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis String 数据结构工具类
 */
@Component
public class RedisStringService extends AbstractRedisService {

    /**
     * 设置 String 值
     */
    public void set(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置对象（JSON 序列化）
     */
    public <T> void setObj(String key, T value) {
        if (value == null) {
            redisTemplate.opsForValue().set(key, "");
        } else if (value instanceof String) {
            redisTemplate.opsForValue().set(key, (String) value);
        } else {
            redisTemplate.opsForValue().set(key, convertToString(value));
        }
    }

    /**
     * 设置 String 值，带过期时间
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 设置对象，带过期时间
     */
    public <T> void setObj(String key, T value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, convertToString(value), timeout, unit);
    }

    /**
     * 获取 String 值
     */
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 获取对象（JSON 反序列化）
     */
    public <T> T getObj(String key, Class<T> clazz) {
        String value = redisTemplate.opsForValue().get(key);
        return convert(value, clazz);
    }

    /**
     * 获取对象（JSON 反序列化）
     */
    public <T> T getObj(String key, Type type) {
        String value = redisTemplate.opsForValue().get(key);
        return convert(value, type);
    }

    /**
     * 返回 key 中字符串值的子字符
     */
    public String getRange(String key, long start, long end) {
        return redisTemplate.opsForValue().get(key, start, end);
    }

    /**
     * 将给定 key 的值设为 value，并返回 key 的旧值
     */
    public String getAndSet(String key, String value) {
        return redisTemplate.opsForValue().getAndSet(key, value);
    }

    /**
     * 批量获取
     */
    public List<String> multiGet(Collection<String> keys) {
        return redisTemplate.opsForValue().multiGet(keys);
    }

    /**
     * 批量获取对象
     */
    public <T> List<T> multiGetObj(Collection<String> keys, Class<T> clazz) {
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        List<T> result = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                result.add(convert(value, clazz));
            }
        }
        return result;
    }

    /**
     * 设置过期时间
     */
    public void setEx(String key, String value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    /**
     * 设置对象，带过期时间
     */
    public <T> void setObjEx(String key, T value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, convertToString(value), timeout, unit);
    }

    /**
     * 只有在 key 不存在时设置
     */
    public boolean setIfAbsent(String key, String value) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value));
    }

    /**
     * 只有在 key 不存在时设置对象
     */
    public <T> boolean setObjIfAbsent(String key, T value) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, convertToString(value)));
    }

    /**
     * 获取字符串的长度
     */
    public Long size(String key) {
        return redisTemplate.opsForValue().size(key);
    }

    /**
     * 批量添加
     */
    public void multiSet(Map<String, String> maps) {
        redisTemplate.opsForValue().multiSet(maps);
    }

    /**
     * 同时设置一个或多个 key-value 对，当且仅当所有给定 key 都不存在
     */
    public boolean multiSetIfAbsent(Map<String, String> maps) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().multiSetIfAbsent(maps));
    }

    /**
     * 增加 (自增长), 负数则为自减
     */
    public Long incrBy(String key, long increment) {
        return redisTemplate.opsForValue().increment(key, increment);
    }

    /**
     * 自增浮点数
     */
    public Double incrByFloat(String key, double increment) {
        return redisTemplate.opsForValue().increment(key, increment);
    }

    /**
     * 追加到末尾
     */
    public Integer append(String key, String value) {
        return redisTemplate.opsForValue().append(key, value);
    }

    /**
     * 获取指定位上的值
     */
    public Boolean getBit(String key, long offset) {
        return redisTemplate.opsForValue().getBit(key, offset);
    }

    /**
     * 设置指定位上的值
     */
    public boolean setBit(String key, long offset, boolean value) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setBit(key, offset, value));
    }

    /**
     * 用 value 参数覆写给定 key 所储存的字符串值，从偏移量 offset 开始
     */
    public void setRange(String key, String value, long offset) {
        redisTemplate.opsForValue().set(key, value, offset);
    }
}