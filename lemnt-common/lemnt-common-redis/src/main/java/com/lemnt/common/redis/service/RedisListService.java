package com.lemnt.common.redis.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis List 数据结构工具类
 */
@Component
public class RedisListService extends AbstractRedisService {

    /**
     * 通过索引获取列表中的元素
     */
    public String lIndex(String key, long index) {
        return redisTemplate.opsForList().index(key, index);
    }

    /**
     * 通过索引获取列表中的对象
     */
    public <T> T lIndexObj(String key, long index, Class<T> clazz) {
        String value = redisTemplate.opsForList().index(key, index);
        return convert(value, clazz);
    }

    /**
     * 获取列表指定范围内的元素
     */
    public List<String> lRange(String key, long start, long end) {
        return redisTemplate.opsForList().range(key, start, end);
    }

    /**
     * 获取列表指定范围内的对象
     */
    public <T> List<T> lRangeObj(String key, long start, long end, Class<T> clazz) {
        List<String> values = redisTemplate.opsForList().range(key, start, end);
        List<T> result = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                result.add(convert(value, clazz));
            }
        }
        return result;
    }

    /**
     * 存储在 list 头部
     */
    public Long lLeftPush(String key, String value) {
        return redisTemplate.opsForList().leftPush(key, value);
    }

    /**
     * 存储在 list 头部（对象）
     */
    public <T> Long lLeftPushObj(String key, T value) {
        return redisTemplate.opsForList().leftPush(key, convertToString(value));
    }

    /**
     * 存储在 list 头部（对象），带过期时间
     */
    public <T> Long lLeftPushObj(String key, T value, long timeout, TimeUnit unit) {
        Long result = redisTemplate.opsForList().leftPush(key, convertToString(value));
        redisTemplate.expire(key, timeout, unit);
        return result;
    }

    /**
     * 批量存储在 list 头部
     */
    public Long lLeftPushAll(String key, String... value) {
        return redisTemplate.opsForList().leftPushAll(key, value);
    }

    /**
     * 批量存储在 list 头部
     */
    public Long lLeftPushAll(String key, Collection<String> value) {
        return redisTemplate.opsForList().leftPushAll(key, value);
    }

    /**
     * 当 list 存在的时候才加入
     */
    public Long lLeftPushIfPresent(String key, String value) {
        return redisTemplate.opsForList().leftPushIfPresent(key, value);
    }

    /**
     * 如果 pivot 存在，在 pivot 前面添加
     */
    public Long lLeftPush(String key, String pivot, String value) {
        return redisTemplate.opsForList().leftPush(key, pivot, value);
    }

    /**
     * 存储在 list 尾部
     */
    public Long lRightPush(String key, String value) {
        return redisTemplate.opsForList().rightPush(key, value);
    }

    /**
     * 存储在 list 尾部（对象）
     */
    public <T> Long lRightPushObj(String key, T value) {
        return redisTemplate.opsForList().rightPush(key, convertToString(value));
    }

    /**
     * 存储在 list 尾部（对象），带过期时间
     */
    public <T> Long lRightPushObj(String key, T value, long timeout, TimeUnit unit) {
        Long result = redisTemplate.opsForList().rightPush(key, convertToString(value));
        redisTemplate.expire(key, timeout, unit);
        return result;
    }

    /**
     * 批量存储在 list 尾部
     */
    public Long lRightPushAll(String key, String... value) {
        return redisTemplate.opsForList().rightPushAll(key, value);
    }

    /**
     * 批量存储在 list 尾部
     */
    public Long lRightPushAll(String key, Collection<String> value) {
        return redisTemplate.opsForList().rightPushAll(key, value);
    }

    /**
     * 为已存在的列表添加值
     */
    public Long lRightPushIfPresent(String key, String value) {
        return redisTemplate.opsForList().rightPushIfPresent(key, value);
    }

    /**
     * 在 pivot 元素的右边添加值
     */
    public Long lRightPush(String key, String pivot, String value) {
        return redisTemplate.opsForList().rightPush(key, pivot, value);
    }

    /**
     * 通过索引设置列表元素的值
     */
    public void lSet(String key, long index, String value) {
        redisTemplate.opsForList().set(key, index, value);
    }

    /**
     * 通过索引设置列表元素的值（对象）
     */
    public <T> void lSetObj(String key, long index, T value) {
        redisTemplate.opsForList().set(key, index, convertToString(value));
    }

    /**
     * 移出并获取列表的第一个元素
     */
    public String lLeftPop(String key) {
        return redisTemplate.opsForList().leftPop(key);
    }

    /**
     * 移出并获取列表的第一个元素（对象）
     */
    public <T> T lLeftPopObj(String key, Class<T> clazz) {
        String value = redisTemplate.opsForList().leftPop(key);
        return convert(value, clazz);
    }

    /**
     * 移出并获取列表的第一个元素（阻塞）
     */
    public String lBLeftPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().leftPop(key, timeout, unit);
    }

    /**
     * 移除并获取列表最后一个元素
     */
    public String lRightPop(String key) {
        return redisTemplate.opsForList().rightPop(key);
    }

    /**
     * 移除并获取列表最后一个元素（对象）
     */
    public <T> T lRightPopObj(String key, Class<T> clazz) {
        String value = redisTemplate.opsForList().rightPop(key);
        return convert(value, clazz);
    }

    /**
     * 移出并获取列表的最后一个元素（阻塞）
     */
    public String lBRightPop(String key, long timeout, TimeUnit unit) {
        return redisTemplate.opsForList().rightPop(key, timeout, unit);
    }

    /**
     * 移除列表的最后一个元素，并将该元素添加到另一个列表并返回
     */
    public String lRightPopAndLeftPush(String sourceKey, String destinationKey) {
        return redisTemplate.opsForList().rightPopAndLeftPush(sourceKey, destinationKey);
    }

    /**
     * 删除集合中值等于 value 的元素
     */
    public Long lRemove(String key, long index, String value) {
        return redisTemplate.opsForList().remove(key, index, value);
    }

    /**
     * 裁剪 list
     */
    public void lTrim(String key, long start, long end) {
        redisTemplate.opsForList().trim(key, start, end);
    }

    /**
     * 获取列表长度
     */
    public Long lLen(String key) {
        return redisTemplate.opsForList().size(key);
    }
}