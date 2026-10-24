package com.lemnt.common.redis.service;

import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis Set 数据结构工具类
 */
@Component
public class RedisSetService extends AbstractRedisService {

    /**
     * set 添加元素
     */
    public Long sAdd(String key, String... values) {
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * set 添加元素（对象）
     */
    @SafeVarargs
    public final <T> Long sAddObj(String key, T... values) {
        String[] strs = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            strs[i] = convertToString(values[i]);
        }
        return redisTemplate.opsForSet().add(key, strs);
    }

    /**
     * set 添加元素（对象），带过期时间
     */
    @SafeVarargs
    public final <T> Long sAddObj(String key, long timeout, TimeUnit unit, T... values) {
        String[] strs = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            strs[i] = convertToString(values[i]);
        }
        Long result = redisTemplate.opsForSet().add(key, strs);
        redisTemplate.expire(key, timeout, unit);
        return result;
    }

    /**
     * set 移除元素
     */
    public Long sRemove(String key, Object... values) {
        return redisTemplate.opsForSet().remove(key, values);
    }

    /**
     * 移除并返回集合的一个随机元素
     */
    public String sPop(String key) {
        return redisTemplate.opsForSet().pop(key);
    }

    /**
     * 移除并返回集合的一个随机元素（对象）
     */
    public <T> T sPopObj(String key, Class<T> clazz) {
        String value = redisTemplate.opsForSet().pop(key);
        return convert(value, clazz);
    }

    /**
     * 将元素 value 从一个集合移到另一个集合
     */
    public Boolean sMove(String key, String value, String destKey) {
        return redisTemplate.opsForSet().move(key, value, destKey);
    }

    /**
     * 获取集合的大小
     */
    public Long sSize(String key) {
        return redisTemplate.opsForSet().size(key);
    }

    /**
     * 判断集合是否包含 value
     */
    public Boolean sIsMember(String key, Object value) {
        return redisTemplate.opsForSet().isMember(key, value);
    }

    /**
     * 获取集合所有元素
     */
    public Set<String> setMembers(String key) {
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * 获取集合所有元素（对象）
     */
    public <T> Set<T> setMembersObj(String key, Class<T> clazz) {
        Set<String> members = redisTemplate.opsForSet().members(key);
        Set<T> result = new HashSet<>();
        if (members != null) {
            for (String member : members) {
                result.add(convert(member, clazz));
            }
        }
        return result;
    }

    /**
     * 随机获取集合中的一个元素
     */
    public String sRandomMember(String key) {
        return redisTemplate.opsForSet().randomMember(key);
    }

    /**
     * 随机获取集合中的一个元素（对象）
     */
    public <T> T sRandomMemberObj(String key, Class<T> clazz) {
        String value = redisTemplate.opsForSet().randomMember(key);
        return convert(value, clazz);
    }

    /**
     * 随机获取集合中 count 个元素
     */
    public List<String> sRandomMembers(String key, long count) {
        return redisTemplate.opsForSet().randomMembers(key, count);
    }

    /**
     * 随机获取集合中 count 个元素（对象）
     */
    public <T> List<T> sRandomMembersObj(String key, long count, Class<T> clazz) {
        List<String> values = redisTemplate.opsForSet().randomMembers(key, count);
        List<T> result = new ArrayList<>();
        if (values != null) {
            for (String value : values) {
                result.add(convert(value, clazz));
            }
        }
        return result;
    }

    /**
     * 随机获取集合中 count 个元素并且去除重复的
     */
    public Set<String> sDistinctRandomMembers(String key, long count) {
        return redisTemplate.opsForSet().distinctRandomMembers(key, count);
    }

    /**
     * 迭代集合
     */
    public Cursor<String> sScan(String key, ScanOptions options) {
        return redisTemplate.opsForSet().scan(key, options);
    }

    /**
     * 获取两个集合的交集
     */
    public Set<String> sIntersect(String key, String otherKey) {
        return redisTemplate.opsForSet().intersect(key, otherKey);
    }

    /**
     * 获取 key 集合与多个集合的交集
     */
    public Set<String> sIntersect(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().intersect(key, otherKeys);
    }

    /**
     * key 集合与 otherKey 集合的交集存储到 destKey 集合中
     */
    public Long sIntersectAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForSet().intersectAndStore(key, otherKey, destKey);
    }

    /**
     * key 集合与多个集合的交集存储到 destKey 集合中
     */
    public Long sIntersectAndStore(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate.opsForSet().intersectAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取两个集合的并集
     */
    public Set<String> sUnion(String key, String otherKeys) {
        return redisTemplate.opsForSet().union(key, otherKeys);
    }

    /**
     * 获取 key 集合与多个集合的并集
     */
    public Set<String> sUnion(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().union(key, otherKeys);
    }

    /**
     * key 集合与 otherKey 集合的并集存储到 destKey 中
     */
    public Long sUnionAndStore(String key, String otherKey, String destKey) {
        return redisTemplate.opsForSet().unionAndStore(key, otherKey, destKey);
    }

    /**
     * key 集合与多个集合的并集存储到 destKey 中
     */
    public Long sUnionAndStore(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate.opsForSet().unionAndStore(key, otherKeys, destKey);
    }

    /**
     * 获取两个集合的差集
     */
    public Set<String> sDifference(String key, String otherKey) {
        return redisTemplate.opsForSet().difference(key, otherKey);
    }

    /**
     * 获取 key 集合与多个集合的差集
     */
    public Set<String> sDifference(String key, Collection<String> otherKeys) {
        return redisTemplate.opsForSet().difference(key, otherKeys);
    }

    /**
     * key 集合与 otherKey 集合的差集存储到 destKey 中
     */
    public Long sDifference(String key, String otherKey, String destKey) {
        return redisTemplate.opsForSet().differenceAndStore(key, otherKey, destKey);
    }

    /**
     * key 集合与多个集合的差集存储到 destKey 中
     */
    public Long sDifference(String key, Collection<String> otherKeys, String destKey) {
        return redisTemplate.opsForSet().differenceAndStore(key, otherKeys, destKey);
    }
}