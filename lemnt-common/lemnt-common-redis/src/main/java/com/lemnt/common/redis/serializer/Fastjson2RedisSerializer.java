package com.lemnt.common.redis.serializer;

import com.alibaba.fastjson2.JSON;

import java.lang.reflect.Type;
import java.math.BigDecimal;

/**
 * fastjson2 Redis 序列化实现（默认）
 *
 * @author wangxx
 */
public class Fastjson2RedisSerializer implements RedisSerializer {

    @Override
    public <T> String serialize(T value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        return JSON.toJSONString(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(String value, Class<T> clazz) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (clazz == String.class) {
            return (T) value;
        }
        if (clazz == Boolean.class || clazz == boolean.class) {
            return (T) Boolean.valueOf(value);
        }
        if (clazz == Integer.class || clazz == int.class) {
            return (T) Integer.valueOf(value);
        }
        if (clazz == Long.class || clazz == long.class) {
            return (T) Long.valueOf(value);
        }
        if (clazz == Double.class || clazz == double.class) {
            return (T) Double.valueOf(value);
        }
        if (clazz == Float.class || clazz == float.class) {
            return (T) Float.valueOf(value);
        }
        if (clazz == Short.class || clazz == short.class) {
            return (T) Short.valueOf(value);
        }
        if (clazz == Byte.class || clazz == byte.class) {
            return (T) Byte.valueOf(value);
        }
        if (clazz == BigDecimal.class) {
            return (T) new BigDecimal(value);
        }
        return JSON.parseObject(value, clazz);
    }

    @Override
    public <T> T deserialize(String value, Type type) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return JSON.parseObject(value, type);
    }
}
