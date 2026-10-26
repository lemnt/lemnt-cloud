package com.lemnt.common.core.constant;

import java.util.concurrent.TimeUnit;

/**
 * 缓存常量信息
 *
 * @author wangxx
 */
public class CacheConstants {
    /**
     * 默认缓存前缀
     */
    public final static String DEF_PREFIX = "lemnt:";

    /**
     * 全局缓存前缀
     */
    public final static String GLOBAL_PREFIX = DEF_PREFIX + "global:";
    /**
     * 登录会话有效期：30 天，单位秒，与 Redis TTL 和 expires_in 一致
     */
    public final static long TOKEN_TTL_SECONDS = TimeUnit.DAYS.toSeconds(30);

    /**
     * 登录会话续期阈值：剩余不超过 120 分钟时续期，单位秒
     */
    public final static long TOKEN_REFRESH_THRESHOLD_SECONDS = TimeUnit.MINUTES.toSeconds(120);

    /**
     * 密码最大错误次数
     */
    public final static int PASSWORD_MAX_RETRY_COUNT = 5;

    /**
     * 密码锁定时间，默认10（分钟）
     */
    public final static long PASSWORD_LOCK_TIME = 10;

    /**
     * 权限缓存前缀
     */
    public final static String LOGIN_TOKEN_KEY = GLOBAL_PREFIX + "login_tokens:";

    /**
     * 验证码 redis key
     */
    public static final String CAPTCHA_CODE_KEY = GLOBAL_PREFIX + "captcha_codes:";

    /**
     * 参数管理 cache key
     */
    public static final String SYS_CONFIG_KEY = GLOBAL_PREFIX + "sys_config:";

    /**
     * 字典管理 cache key
     */
    public static final String SYS_DICT_KEY = GLOBAL_PREFIX + "sys_dict:";

    /**
     * 登录账户密码错误次数 redis key
     */
    public static final String PWD_ERR_CNT_KEY = GLOBAL_PREFIX + "pwd_err_cnt:";

    /**
     * 登录IP黑名单 cache key
     */
    public static final String SYS_LOGIN_BLACKIPLIST = SYS_CONFIG_KEY + "sys.login.blackIPList";

    /**
     * 客户端账户缓存
     */
    public static final String CLIENT_ACCOUNT_KEY = GLOBAL_PREFIX + "client_account";

    /**
     * 行业树缓存
     */
    public static final String INDUSTRY_KEY = GLOBAL_PREFIX + "industry";

    /**
     * 雪花算法worker缓存
     */
    public static final String SNOWFLAKE_WORKER_KEY = GLOBAL_PREFIX + "snowflake:%s:%s:%d:%d";

}
