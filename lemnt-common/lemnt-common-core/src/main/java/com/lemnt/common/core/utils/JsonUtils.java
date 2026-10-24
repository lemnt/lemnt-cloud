package com.lemnt.common.core.utils;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * JSON 工具类
 *
 * @author wangxx
 */
public class JsonUtils {

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param object 对象
     * @return JSON 字符串
     */
    public static String toJsonStr(Object object) {
        if (ObjectUtil.isNull(object)) {
            return StringUtils.EMPTY;
        }
        return JSON.toJSONString(object);
    }

    /**
     * 将对象转换为 JSON 字符串（带格式化）
     *
     * @param object 对象
     * @return 格式化后的 JSON 字符串
     */
    public static String toPrettyJson(Object object) {
        if (ObjectUtil.isNull(object)) {
            return StringUtils.EMPTY;
        }
        return JSON.toJSONString(object, JSONWriter.Feature.PrettyFormat);
    }

    /**
     * 将 List 转换为 JSON 字符串
     *
     * @param list 列表
     * @return JSON 字符串
     */
    public static <T> String toJsonStr(List<T> list) {
        if (CollUtil.isEmpty(list)) {
            return StringUtils.EMPTY;
        }
        return JSON.toJSONString(list);
    }

    /**
     * 将 JSON 字符串转换为对象
     *
     * @param text  JSON 字符串
     * @param clazz 目标类型
     * @return 对象
     */
    public static <T> T toBean(String text, Class<T> clazz) {
        if (StringUtils.isEmpty(text)) {
            return null;
        }
        return JSON.parseObject(text, clazz);
    }

    /**
     * 将字节数组转换为对象
     *
     * @param bytes 字节数组
     * @param clazz 目标类型
     * @return 对象
     */
    public static <T> T toBean(byte[] bytes, Class<T> clazz) {
        if (ArrayUtil.isEmpty(bytes)) {
            return null;
        }
        return JSON.parseObject(bytes, clazz);
    }

    /**
     * 将 JSON 字符串转换为对象（支持泛型）
     *
     * @param text          JSON 字符串
     * @param typeReference 类型引用
     * @return 对象
     */
    public static <T> T toBean(String text, TypeReference<T> typeReference) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        return JSON.parseObject(text, typeReference);
    }

    /**
     * 将 JSON 字符串转换为 List
     *
     * @param text  JSON 字符串
     * @param clazz 元素类型
     * @return List
     */
    public static <T> List<T> toList(String text, Class<T> clazz) {
        if (StringUtils.isEmpty(text)) {
            return new ArrayList<>();
        }
        return JSON.parseArray(text, clazz);
    }

    /**
     * 将 JSONArray 转换为 List
     *
     * @param array JSONArray
     * @param clazz 元素类型
     * @return List
     */
    public static <T> List<T> toList(JSONArray array, Class<T> clazz) {
        if (CollUtil.isEmpty(array)) {
            return new ArrayList<>();
        }
        return array.toJavaList(clazz);
    }

    /**
     * 将 JSONArray 转换为 List
     *
     * @param text          JSONArray
     * @param typeReference 元素类型
     * @return List
     */
    public static <T> List<T> toTypeList(String text, TypeReference<List<T>> typeReference) {
        if (StringUtils.isEmpty(text)) {
            return new ArrayList<>();
        }
        try {
            return JSON.parseObject(text, typeReference);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 将 JSON 字符串转换为 Map
     *
     * @param text JSON 字符串
     * @return Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        return JSON.parseObject(text, Map.class);
    }

    /**
     * 将对象转换为 Map
     *
     * @param object 对象
     * @return Map
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> toMap(Object object) {
        if (ObjectUtil.isNull(object)) {
            return null;
        }
        String json = JSON.toJSONString(object);
        return JSON.parseObject(json, Map.class);
    }

    /**
     * 将 JSON 字符串转换为 JSONObject
     *
     * @param text JSON 字符串
     * @return Map
     */
    public static JSONObject toJsonObj(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        return JSON.parseObject(text, JSONObject.class);
    }
}