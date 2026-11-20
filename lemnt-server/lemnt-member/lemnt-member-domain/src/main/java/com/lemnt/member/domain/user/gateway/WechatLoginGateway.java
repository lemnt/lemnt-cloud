package com.lemnt.member.domain.user.gateway;

/**
 * 微信小程序登录边界：用一次性登录凭证换取 openid，不在本服务保存会话密钥。
 */
public interface WechatLoginGateway {

    /**
     * 换取 openid；凭证无效、过期或渠道返回错误时抛出异常。
     */
    String exchangeOpenId(String code);
}
