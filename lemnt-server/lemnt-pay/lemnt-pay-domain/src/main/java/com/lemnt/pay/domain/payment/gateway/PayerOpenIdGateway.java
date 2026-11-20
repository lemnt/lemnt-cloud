package com.lemnt.pay.domain.payment.gateway;

/**
 * 付款人渠道标识查询边界：微信支付需要会员在小程序下绑定的 openid。
 */
public interface PayerOpenIdGateway {

    /**
     * 查询会员绑定的微信小程序 openid，未绑定时返回 null。
     */
    String findOpenId(Long memberId);
}
