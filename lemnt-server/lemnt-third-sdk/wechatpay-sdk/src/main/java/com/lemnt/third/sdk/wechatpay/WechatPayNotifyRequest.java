package com.lemnt.third.sdk.wechatpay;

import java.util.Map;

/**
 * 微信支付回调的验签头与报文原文。字段缺失时直接失败，不进入验签。
 */
public record WechatPayNotifyRequest(
    String serialNumber,
    String signature,
    String timestamp,
    String nonce,
    String signType,
    String body) {

    private static final String HEADER_SERIAL = "Wechatpay-Serial";
    private static final String HEADER_SIGNATURE = "Wechatpay-Signature";
    private static final String HEADER_TIMESTAMP = "Wechatpay-Timestamp";
    private static final String HEADER_NONCE = "Wechatpay-Nonce";
    private static final String HEADER_SIGN_TYPE = "Wechatpay-Signature-Type";

    public WechatPayNotifyRequest {
        requireText(serialNumber, HEADER_SERIAL);
        requireText(signature, HEADER_SIGNATURE);
        requireText(timestamp, HEADER_TIMESTAMP);
        requireText(nonce, HEADER_NONCE);
        requireText(signType, HEADER_SIGN_TYPE);
        requireText(body, "通知报文");
    }

    /**
     * 从 HTTP 请求头提取验签参数，请求头名称大小写不敏感。
     *
     * @param headers 请求头
     * @param body    原始报文
     * @return 验签参数
     */
    public static WechatPayNotifyRequest fromHeaders(Map<String, String> headers, String body) {
        if (headers == null) {
            throw new IllegalArgumentException("微信支付通知缺少请求头");
        }
        return new WechatPayNotifyRequest(
            header(headers, HEADER_SERIAL),
            header(headers, HEADER_SIGNATURE),
            header(headers, HEADER_TIMESTAMP),
            header(headers, HEADER_NONCE),
            header(headers, HEADER_SIGN_TYPE),
            body);
    }

    private static String header(Map<String, String> headers, String name) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().equalsIgnoreCase(name)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("微信支付通知缺少" + name);
        }
    }
}
