package com.lemnt.common.redis.service;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

/**
 * Redis Hash 数据结构工具类
 */
@Component
public class RedisHashService extends AbstractRedisService {

    /**
     * 获取存储在哈希表中指定字段的值
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 获取存储在哈希表中指定字段的对象
     */
    public <T> T hGetObj(String key, String field, Class<T> clazz) {
        Object value = redisTemplate.opsForHash().get(key, field);
        if (value == null) {
            return null;
        }
        return convert(String.valueOf(value), clazz);
    }

    /**
     * 获取所有给定字段的值
     */
    public Map<String, String> hGetAll(String key) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        Map<String, String> result = new HashMap<>();
        for (Entry<Object, Object> entry : entries.entrySet()) {
            Object value = entry.getValue();
            result.put(String.valueOf(entry.getKey()), String.valueOf(value));
        }
        return result;
    }

    /**
     * 获取所有给定字段的对象
     */
    public <T> Map<String, T> hGetAllObj(String key, Class<T> clazz) {
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(key);
        Map<String, T> result = new HashMap<>();
        for (Entry<Object, Object> entry : entries.entrySet()) {
            Object value = entry.getValue();
            if (clazz.isInstance(value)) {
                result.put(String.valueOf(entry.getKey()), clazz.cast(value));
            } else {
                result.put(String.valueOf(entry.getKey()), convert(String.valueOf(value), clazz));
            }
        }
        return result;
    }

    /**
     * 获取所有给定字段的值
     */
    public List<Object> hMultiGet(String key, Collection<Object> fields) {
        return redisTemplate.opsForHash().multiGet(key, fields);
    }

    /**
     * 设置哈希表键值
     */
    public void hPut(String key, String hashKey, String value) {
        redisTemplate.opsForHash().put(key, hashKey, value);
    }

    /**
     * 设置哈希表键值（对象）
     */
    public <T> void hPutObj(String key, String hashKey, T value) {
        redisTemplate.opsForHash().put(key, hashKey, convertToString(value));
    }

    /**
     * 设置哈希表键值，并设置过期时间
     */
    public <T> void hPutObj(String key, String hashKey, T value, long timeout, TimeUnit unit) {
        redisTemplate.opsForHash().put(key, hashKey, convertToString(value));
        redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 批量设置哈希表键值
     */
    public void hPutAll(String key, Map<String, String> maps) {
        redisTemplate.opsForHash().putAll(key, maps);
    }

    /**
     * 仅当 hashKey 不存在时才设置
     */
    public Boolean hPutIfAbsent(String key, String hashKey, String value) {
        return redisTemplate.opsForHash().putIfAbsent(key, hashKey, value);
    }

    /**
     * 仅当 hashKey 不存在时才设置对象
     */
    public <T> Boolean hPutObjIfAbsent(String key, String hashKey, T value) {
        return redisTemplate.opsForHash().putIfAbsent(key, hashKey, convertToString(value));
    }

    /**
     * 删除一个或多个哈希表字段
     */
    public Long hDelete(String key, Object... fields) {
        return redisTemplate.opsForHash().delete(key, fields);
    }

    /**
     * 查看哈希表 key 中，指定的字段是否存在
     */
    public boolean hExists(String key, String field) {
        return redisTemplate.opsForHash().hasKey(key, field);
    }

    /**
     * 为哈希表 key 中的指定字段的整数值加上增量 increment
     */
    public Long hIncrBy(String key, Object field, long increment) {
        return redisTemplate.opsForHash().increment(key, field, increment);
    }

    /**
     * 为哈希表 key 中的指定字段的浮点数值加上增量
     */
    public Double hIncrByFloat(String key, Object field, double delta) {
        return redisTemplate.opsForHash().increment(key, field, delta);
    }

    /**
     * 获取所有哈希表中的字段
     */
    public Set<Object> hKeys(String key) {
        return redisTemplate.opsForHash().keys(key);
    }

    /**
     * 获取哈希表中字段的数量
     */
    public Long hSize(String key) {
        return redisTemplate.opsForHash().size(key);
    }

    /**
     * 获取哈希表中所有值
     */
    public List<Object> hValues(String key) {
        return redisTemplate.opsForHash().values(key);
    }

    /**
     * 获取哈希表中所有对象
     */
    public <T> List<T> hValuesObj(String key, Class<T> clazz) {
        List<Object> values = redisTemplate.opsForHash().values(key);
        List<T> result = new ArrayList<>();
        for (Object value : values) {
            if (clazz.isInstance(value)) {
                result.add(clazz.cast(value));
            } else {
                result.add(convert(String.valueOf(value), clazz));
            }
        }
        return result;
    }

    /**
     * 迭代哈希表中的键值对
     */
    public Cursor<Entry<Object, Object>> hScan(String key, ScanOptions options) {
        return redisTemplate.opsForHash().scan(key, options);
    }
}