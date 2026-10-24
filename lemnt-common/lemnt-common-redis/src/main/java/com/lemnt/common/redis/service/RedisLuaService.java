package com.lemnt.common.redis.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Redis Lua 脚本工具类
 */
@Component
public class RedisLuaService extends AbstractRedisService {

    /**
     * 执行 Lua 脚本（指定返回类型）
     *
     * @param script     Lua 脚本内容
     * @param returnType 返回类型
     * @param keys       key 列表
     * @param args       参数列表
     * @return 执行结果
     */
    public <T> T execute(String script, Class<T> returnType, List<String> keys, List<Object> args) {
        RedisScript<T> redisScript = new DefaultRedisScript<>(script, returnType);
        return redisTemplate.execute(redisScript, keys != null ? keys : Collections.emptyList(), args);
    }

    /**
     * 执行 Lua 脚本（从 classpath 加载脚本，指定返回类型）
     *
     * @param classpath  脚本路径
     * @param returnType 返回类型
     * @param keys       key 列表
     * @param args       参数列表
     * @return 执行结果
     */
    public <T> T executeFromClasspath(String classpath, Class<T> returnType, List<String> keys, List<Object> args) {
        DefaultRedisScript<T> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(classpath)));
        redisScript.setResultType(returnType);
        return redisTemplate.execute(redisScript, keys != null ? keys : Collections.emptyList(), args);
    }
}