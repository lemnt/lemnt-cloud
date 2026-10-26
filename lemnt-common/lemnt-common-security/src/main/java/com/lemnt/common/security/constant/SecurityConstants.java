package com.lemnt.common.security.constant;

/**
 * 权限相关通用常量
 *
 * @author wangxx
 */
public class SecurityConstants {
    /** 服务端签发的登录身份类型，会员与后台不可互用。 */
    public static final String USER_TYPE = "user_type";
    /** 后台用户身份类型。 */
    public static final String ADMIN_USER_TYPE = "admin";
    /** 小程序会员身份类型。 */
    public static final String MEMBER_USER_TYPE = "member";

    /**
     * 用户ID字段
     */
    public static final String DETAILS_USER_ID = "user_id";

    /**
     * 用户名字段
     */
    public static final String DETAILS_USERNAME = "username";

    /**
     * 角色
     */
    public static final String ROLE_KEY = "role_key";

    /**
     * 用户标识
     */
    public static final String USER_KEY = "user_key";

    /**
     * 登录用户
     */
    public static final String LOGIN_USER = "login_user";

    /**
     * 角色权限
     */
    public static final String ROLE_PERMISSION = "role_permission";

    /**
     * 链路追踪ID
     */
    public static final String TRACE_ID = "trace_id";

    /**
     * 授权信息字段
     */
    public static final String AUTHORIZATION_HEADER = "Authorization";

    public static final String NONCE_HEADER = "X-Nonce";

    public static final String SIGNATURE_HEADER = "X-Signature";

    public static final String CLIENT_ID_HEADER = "X-Client-Id";

    public static final String TIMESTAMP_HEADER = "X-Timestamp";

    public static final String HEADER_TRACE_ID = "X-Trace-Id";

    public static final String HEADER_USER_KEY = "X-User-Key";

    public static final String HEADER_USER_ID = "X-User-Id";

    public static final String HEADER_USER_NAME = "X-User-Name";

    public static final String HEADER_ROLE_KEY = "X-Role-Key";

    public static final String HEADER_ROLE_PERMISSION = "X-Role-Permission";

    public static final String HEADER_LOGIN_USER = "X-Login-User";

}
