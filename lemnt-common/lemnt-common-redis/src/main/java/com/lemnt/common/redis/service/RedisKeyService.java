package com.lemnt.common.redis.service;

import org.springframework.data.redis.connection.DataType;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis Key 基础操作工具类
 */
@Component
public class RedisKeyService extends AbstractRedisService {

    /**
     * 批量删除 key
     */
    public void delete(Collection<String> keys) {
        redisTemplate.delete(keys);
    }

    /**
     * 设置过期时间（指定日期）
     */
    public Boolean expireAt(String key, Date date) {
        return redisTemplate.expireAt(key, date);
    }

    /**
     * 移除 key 的过期时间
     */
    public Boolean persist(String key) {
        return redisTemplate.persist(key);
    }

    /**
     * 返回 key 的剩余的过期时间（指定单位）
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }

    /**
     * 返回 key 的剩余的过期时间（秒）
     */
    public Long getExpire(String key) {
        return redisTemplate.getExpire(key);
    }

    /**
     * 返回 key 所储存的值的类型
     */
    public DataType type(String key) {
        return redisTemplate.type(key);
    }

    /**
     * 将当前数据库的 key 移动到给定的数据库
     */
    public Boolean move(String key, int dbIndex) {
        return redisTemplate.move(key, dbIndex);
    }

    /**
     * 从当前数据库中随机返回一个 key
     */
    public String randomKey() {
        return redisTemplate.randomKey();
    }

    /**
     * 修改 key 的名称
     */
    public void rename(String oldKey, String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    /**
     * 仅当 newKey 不存在时，将 oldKey 改名为 newKey
     */
    public Boolean renameIfAbsent(String oldKey, String newKey) {
        return redisTemplate.renameIfAbsent(oldKey, newKey);
    }

    /**
     * 序列化 key
     */
    public byte[] dump(String key) {
        return redisTemplate.dump(key);
    }
}