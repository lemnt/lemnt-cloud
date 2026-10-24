package com.lemnt.common.redis.service;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis ZSet (有序集合) 数据结构工具类
 */
@Component
public class RedisZSetService extends AbstractRedisService {

    /**
     * 添加元素，有序集合按照元素的 score 值由小到大排列
     */
    public Boolean zAdd(String key, String value, double score) {
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * 添加元素（对象）
     */
    public <T> Boolean zAddObj(String key, T value, double score) {
        return redisTemplate.opsForZSet().add(key, convertToString(value), score);
    }

    /**
     * 添加元素（对象），带过期时间
     */
    public <T> Boolean zAddObj(String key, T value, double score, long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.opsForZSet().add(key, convertToString(value), score);
        redisTemplate.expire(key, timeout, unit);
        return result;
    }

    /**
     * 批量添加元素
     */
    public Long zAdd(String key, Set<TypedTuple<String>> values) {
        return redisTemplate.opsForZSet().add(key, values);
    }

    /**
     * 移除元素
     */
    public Long zRemove(String key, Object... values) {
        return redisTemplate.opsForZSet().remove(key, values);
    }

    /**
     * 增加元素的 score 值，并返回增加后的值
     */
    public Double zIncrementScore(String key, String value, double delta) {
        return redisTemplate.opsForZSet().incrementScore(key, value, delta);
    }

    /**
     * 返回元素在集合的排名，有序集合按照元素的 score 值由小到大排列
     */
    public Long zRank(String key, Object value) {
        return redisTemplate.opsForZSet().rank(key, value);
    }

    /**
     * 返回元素在集合的排名，按元素的 score 值由大到小排列
     */
    public Long zReverseRank(String key, Object value) {
        return redisTemplate.opsForZSet().reverseRank(key, value);
    }

    /**
     * 获取集合的元素，从小到大排序
     */
    public Set<String> zRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().range(key, start, end);
    }

    /**
     * 获取集合的元素，从小到大排序（对象）
     */
    public <T> Set<T> zRangeObj(String key, long start, long end, Class<T> clazz) {
        Set<String> values = redisTemplate.opsForZSet().range(key, start, end);
        Set<T> result = new LinkedHashSet<>();
        if (values != null) {
            for (String value : values) {
                result.add(convert(value, clazz));
            }
        }
        return result;
    }

    /**
     * 获取集合元素，并且把 score 值也获取
     */
    public Set<TypedTuple<String>> zRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().rangeWithScores(key, start, end);
    }

    /**
     * 根据 Score 值查询集合元素
     */
    public Set<String> zRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScore(key, min, max);
    }

    /**
     * 根据 Score 值查询集合元素，从小到大排序
     */
    public Set<TypedTuple<String>> zRangeByScoreWithScores(String key, double min, double max) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max);
    }

    public Set<TypedTuple<String>> zRangeByScoreWithScores(String key, double min, double max, long start, long end) {
        return redisTemplate.opsForZSet().rangeByScoreWithScores(key, min, max, start, end);
    }

    /**
     * 获取集合的元素，从大到小排序
     */
    public Set<String> zReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * 获取集合的元素，从大到小排序（对象）
     */
    public <T> Set<T> zReverseRangeObj(String key, long start, long end, Class<T> clazz) {
        Set<String> values = redisTemplate.opsForZSet().reverseRange(key, start, end);
        Set<T> result = new LinkedHashSet<>();
        if (values != null) {
            for (String value : values) {
                result.add(convert(value, clazz));
            }
        }
        return result;
    }

    /**
     * 获取集合的元素，从大到小排序，并返回 score 值
     */
    public Set<TypedTuple<String>> zReverseRangeWithScores(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    /**
     * 根据 Score 值查询集合元素，从大到小排序
     */
    public Set<String> zReverseRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max);
    }

    public Set<TypedTuple<String>> zReverseRangeByScoreWithScores(String key, double min, double max) {
        return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max);
    }

    public Set<String> zReverseRangeByScore(String key, double min, double max, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeByScore(key, min, max, start, end);
    }

    /**
     * 根据 score 值获取集合元素数量
     */
    public Long zCount(String key, double min, double max) {
        return redisTemplate.opsForZSet().count(key, min, max);
    }

    /**
     * 获取集合大小
     */
    public Long zSize(String key) {
        return redisTemplate.opsForZSet().size(key);
    }

    /**
     * 获取集合大小（zCard）
     */
    public Long zZCard(String key) {
        return redisTemplate.opsForZSet().zCard(key);
    }

    /**
     * 获取集合中 value 元素的 score 值
     */
    public Double zScore(String key, Object value) {
        return redisTemplate.opsForZSet().score(key, value);
    }

    /**
     * 移除指定索引位置的成员
     */
    public Long zRemoveRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().removeRange(key, start, end);
    }

    /**
     * 根据指定的 score 值的范围来移除成员
     */
    public Long zRemoveRangeByScore(String key, double min, double max) {
        return redisTemplate.opsForZSet().removeRangeByScore(key, min, max);
    }

    /**
     * 获取 key 和 otherKey 的并集并存储在 destKey 中
     */
    public Long zUnionAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForZSet().unionAndStore(key, otherKey, destKey);
    }

    public Long zUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate.opsForZSet().unionAndStore(key, otherKeys, destKey);
    }

    /**
     * 交集
     */
    public Long zIntersectAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForZSet().intersectAndStore(key, otherKey, destKey);
    }

    public Long zIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate.opsForZSet().intersectAndStore(key, otherKeys, destKey);
    }

    /**
     * 迭代有序集合
     */
    public Cursor<TypedTuple<String>> zScan(String key, ScanOptions options) {
        return redisTemplate.opsForZSet().scan(key, options);
    }
}