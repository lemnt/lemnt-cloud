package com.lemnt.auth.domain.member.gateway;

import com.lemnt.auth.domain.member.model.MemberToken;

/**
 * 会员会话生命周期边界，沿用后台公共令牌组件。
 */
public interface MemberTokenGateway {
    /**
     * 创建。
     */
    MemberToken create(Long memberId, String username);
    /**
     * 会员编号。
     */
    Long memberId(String accessToken);
    /**
     * 续期。
     */
    MemberToken refresh(String accessToken);
    /**
     * 退出。
     */
    void logout(String accessToken);
}
