package com.lemnt.auth.api;

/**
 * 会员会话校验和撤销契约；后台 JWT 不属于此身份域。
 */
public interface MemberSessionClient {
    /**
     * 校验会员访问令牌，失效时返回 null。
     */
    Long authenticate(String accessToken);
    /** 后台停用会员时同步撤销全部会员会话，不影响同ID后台用户。 */
    void revokeMemberSessions(Long memberId);

}
