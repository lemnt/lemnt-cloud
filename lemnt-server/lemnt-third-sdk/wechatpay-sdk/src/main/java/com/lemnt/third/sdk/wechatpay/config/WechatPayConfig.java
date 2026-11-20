package com.lemnt.third.sdk.wechatpay.config;

import lombok.Data;

/**
 * 微信支付 APIv3 渠道配置，字段与支付渠道配置 JSON 一一对应。
 * secret、salt 为历史渠道配置保留字段，APIv3 签名与验签不使用。
 */
@Data
public class WechatPayConfig {

    /**
     * APIv3 密钥长度固定 32 位，用于通知报文解密。
     */
    private static final int API_V3_KEY_LENGTH = 32;

    private String appId;
    private String secret;
    private String salt;
    private String mchId;
    /**
     * 商户 API 证书序列号。
     */
    private String serialNo;
    private String apiV3key;
    /**
     * 商户 API 私钥（apiclient_key.pem 的完整内容），用于请求签名。
     */
    private String privateKey;

    /** 微信支付商户平台下载的微信支付公钥 PEM 内容。 */
    private String publicKey;

    /** 微信支付公钥 ID，对应微信通知的 Wechatpay-Serial 请求头。 */
    private String publicKeyId;

    private String payNotifyUrl;
    private String refundNotifyUrl;

    /**
     * 校验调用微信支付接口必需的字段；缺失时无法完成签名或验签，直接失败而不是发出无效请求。
     */
    public void requireValid() {
        requireText(appId, "appId");
        requireText(mchId, "mchId");
        requireText(serialNo, "serialNo");
        requireText(privateKey, "privateKey");
        requireText(publicKey, "publicKey");
        requireText(publicKeyId, "publicKeyId");
        requireText(apiV3key, "apiV3key");
        if (apiV3key.length() != API_V3_KEY_LENGTH) {
            throw new IllegalArgumentException("微信支付配置 apiV3key 长度必须为32位");
        }
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("微信支付配置缺少" + name);
        }
    }
}
