package com.lemnt.common.security.context;

import cn.hutool.core.convert.Convert;
import com.alibaba.ttl.TransmittableThreadLocal;
import com.lemnt.common.core.utils.StringUtils;
import com.lemnt.common.security.constant.SecurityConstants;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 获取当前线程变量中的 用户id、用户名称、Token等信息
 * 注意： 必须在网关通过请求头的方法传入，同时在HeaderInterceptor拦截器设置值。 否则这里无法获取
 *
 * @author wangxx
 */
public class SecurityContextHolder {
    private static final TransmittableThreadLocal<Map<String, Object>> THREAD_LOCAL = new TransmittableThreadLocal<>();

    public static void set(String key, Object value) {
        Map<String, Object> map = getLocalMap();
        map.put(key, value == null ? StringUtils.EMPTY : value);
    }

    public static String get(String key) {
        Map<String, Object> map = getLocalMap();
        return Convert.toStr(map.getOrDefault(key, StringUtils.EMPTY));
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(String key, Class<T> clazz) {
        Map<String, Object> map = getLocalMap();
        Object value = map.get(key);
        if (clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public static Map<String, Object> getLocalMap() {
        Map<String, Object> map = THREAD_LOCAL.get();
        if (map == null) {
            map = new ConcurrentHashMap<>();
            THREAD_LOCAL.set(map);
        }
        return map;
    }

    public static void setLocalMap(Map<String, Object> threadLocalMap) {
        THREAD_LOCAL.set(threadLocalMap);
    }

    public static Long getUserId() {
        return Convert.toLong(get(SecurityConstants.DETAILS_USER_ID), 0L);
    }

    public static void setUserId(String account) {
        set(SecurityConstants.DETAILS_USER_ID, account);
    }

    public static String getUserName() {
        return get(SecurityConstants.DETAILS_USERNAME);
    }

    public static void setUserName(String username) {
        set(SecurityConstants.DETAILS_USERNAME, username);
    }

    public static String getUserKey() {
        return get(SecurityConstants.USER_KEY);
    }

    public static void setUserKey(String userKey) {
        set(SecurityConstants.USER_KEY, userKey);
    }

    public static String getPermission() {
        return get(SecurityConstants.ROLE_PERMISSION);
    }

    public static void setPermission(String permissions) {
        set(SecurityConstants.ROLE_PERMISSION, permissions);
    }

    public static Set<String> getRoleKey() {
        String roleKey = get(SecurityConstants.ROLE_KEY);
        if (StringUtils.isEmpty(roleKey)) {
            return new HashSet<>();
        }
        return Arrays.stream(roleKey.split(","))
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toSet());
    }

    public static void setRoleKey(String roleKey) {
        set(SecurityConstants.ROLE_KEY, roleKey);
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
