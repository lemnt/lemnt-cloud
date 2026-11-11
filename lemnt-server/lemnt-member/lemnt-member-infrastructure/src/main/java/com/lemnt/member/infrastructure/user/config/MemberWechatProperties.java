package com.lemnt.member.infrastructure.user.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

/**
 * 微信小程序应用凭证，只用于 wx.login 的 code 换取 openid。
 * appId 必须与支付渠道配置中的 appId 一致，否则 JSAPI 支付会因应用不匹配失败。
 */
@Data
@Component
@ConfigurationProperties(prefix = "member.wechat")
public class MemberWechatProperties {

    /**
     * 小程序 AppID。
     */
    private String appId;

    /**
     * 小程序 AppSecret。
     */
    private String secret;
}
