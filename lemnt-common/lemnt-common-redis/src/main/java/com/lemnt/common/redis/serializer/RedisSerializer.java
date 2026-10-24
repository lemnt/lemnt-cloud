package com.lemnt.common.redis.serializer;

import java.lang.reflect.Type;

/**
 * Redis 序列化接口
 * <p>
 * 支持切换不同的 JSON 序列化实现（fastjson2 / Jackson）。
 * </p>
 *
 * @author wangxx
 */
public interface RedisSerializer {

    /**
     * 将对象序列化为 JSON 字符串
     */
    <T> String serialize(T value);

    /**
     * 将 JSON 字符串反序列化为指定类型的对象
     */
    <T> T deserialize(String value, Class<T> clazz);

    /**
     * 将 JSON 字符串反序列化为指定泛型类型的对象
     */
    <T> T deserialize(String value, Type type);
}
