package com.lemnt.auth.api;

/**
 * 会员会话校验契约；后台 JWT 不属于此身份域。
 */
public interface MemberSessionClient {
    /**
     * 校验会员访问令牌，失效时返回 null。
     */
    Long authenticate(String accessToken);
}
