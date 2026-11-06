package com.lemnt.member.domain.user.gateway;

/**
 * 会员微信小程序标识存储边界：一个会员对应一个小程序 openid。
 */
public interface MemberWechatGateway {

    /**
     * 查询会员绑定的 openid，未绑定时返回 null。
     */
    String findOpenId(Long memberId);

    /**
     * 绑定或覆盖会员的 openid；openid 已属于其他会员时明确失败。
     */
    void bind(Long memberId, String openId);
}
