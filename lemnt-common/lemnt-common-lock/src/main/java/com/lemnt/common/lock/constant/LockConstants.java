package com.lemnt.common.lock.constant;

/**
 * 缓存常量信息
 *
 * @author wangxx
 */
public class LockConstants {
    /**
     * 缓存有效期，默认720（分钟）
     */
    public static final long EXPIRATION = 720;

    /**
     * 权限缓存前缀
     */
    public static final String LOGIN_TOKEN_KEY = "login_tokens:";

    public static final String RTB_IMPRESSION_CALLBACK = "lemnt:lock:rtb_impression_callback:";
}
