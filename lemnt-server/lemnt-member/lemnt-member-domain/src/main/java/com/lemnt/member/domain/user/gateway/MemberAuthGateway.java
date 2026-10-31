package com.lemnt.member.domain.user.gateway;

/** 会员状态变更通知认证中心的边界，会员服务不处理令牌。 */
public interface MemberAuthGateway {

    /** 同步要求认证中心撤销该会员全部会话，失败时抛出异常。 */
    void revokeSessions(Long memberId);
}
