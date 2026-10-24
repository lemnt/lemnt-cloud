package com.lemnt.common.snowflake.utils;

import cn.hutool.core.lang.Snowflake;

import java.util.ArrayList;
import java.util.List;

/**
 * 雪花工具类
 *
 * @author wangxx
 */
public class SnowflakeUtils {

    private final Snowflake snowflake;

    public SnowflakeUtils(Snowflake snowflake) {
        this.snowflake = snowflake;
    }

    // 单个 ID
    public long generate() {
        return snowflake.nextId();
    }

    // 单个 ID 字符串
    public String generateStr() {
        return snowflake.nextIdStr();
    }

    // 批量生成 long List
    public List<Long> generateBatch(long count) {
        List<Long> ids = new ArrayList<>((int) count);
        long lastId = snowflake.nextId();
        ids.add(lastId);
        for (int i = 1; i < count; i++) {
            ids.add(lastId + i); // 顺序递增
        }
        return ids;
    }

    // 批量生成字符串 List
    public List<String> generateBatchStr(long count) {
        List<String> ids = new ArrayList<>((int) count);
        for (Long id : generateBatch(count)) {
            ids.add(String.valueOf(id));
        }
        return ids;
    }

    // 批量生成 long[]
    public long[] generateBatchArray(long count) {
        return generateBatch(count).stream().mapToLong(Long::longValue).toArray();
    }

    // 带固定长度字符串（前补零）
    public String generateStrWithLength(int length) {
        String id = snowflake.nextIdStr();
        if (id.length() >= length) return id;
        return String.format("%0" + length + "d", Long.parseLong(id));
    }

    // 带前缀
    public String generateWithPrefix(String prefix) {
        return prefix + snowflake.nextIdStr();
    }

    // 带后缀
    public String generateWithSuffix(String suffix) {
        return snowflake.nextIdStr() + suffix;
    }

    // 带前后缀
    public String generateWithPrefixSuffix(String prefix, String suffix) {
        return prefix + snowflake.nextIdStr() + suffix;
    }
}
