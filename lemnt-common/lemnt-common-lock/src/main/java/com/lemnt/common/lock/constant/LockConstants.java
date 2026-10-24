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
    public final static long EXPIRATION = 720;

    /**
     * 权限缓存前缀
     */
    public final static String LOGIN_TOKEN_KEY = "login_tokens:";

    public final static String RTB_IMPRESSION_CALLBACK = "lemnt:lock:rtb_impression_callback:";

}
