package com.lemnt.third.sdk.wechatpay;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 支付通知请求头的提取与必填校验。
 */
class WechatPayNotifyRequestTest {

    @Test
    void fromHeadersReadsHeadersIgnoringCase() {
        WechatPayNotifyRequest notify = WechatPayNotifyRequest.fromHeaders(headers(), "{}");
        assertEquals("SERIAL-1", notify.serialNumber());
        assertEquals("SIGN-1", notify.signature());
        assertEquals("1700000000", notify.timestamp());
        assertEquals("NONCE-1", notify.nonce());
        assertEquals("WECHATPAY2-SHA256-RSA2048", notify.signType());
        assertEquals("{}", notify.body());
    }

    @Test
    void fromHeadersRejectsMissingSignature() {
        Map<String, String> headers = headers();
        headers.remove("wechatpay-signature");
        IllegalArgumentException exception =
            assertThrows(
                IllegalArgumentException.class,
                () -> WechatPayNotifyRequest.fromHeaders(headers, "{}"));
        assertTrue(exception.getMessage().contains("Wechatpay-Signature"));
    }

    @Test
    void fromHeadersRejectsBlankBody() {
        assertThrows(
            IllegalArgumentException.class,
            () -> WechatPayNotifyRequest.fromHeaders(headers(), " "));
    }

    private Map<String, String> headers() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("wechatpay-serial", "SERIAL-1");
        headers.put("wechatpay-signature", "SIGN-1");
        headers.put("wechatpay-timestamp", "1700000000");
        headers.put("wechatpay-nonce", "NONCE-1");
        headers.put("wechatpay-signature-type", "WECHATPAY2-SHA256-RSA2048");
        return headers;
    }
}
