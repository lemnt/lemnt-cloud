package com.lemnt.third.sdk.wechatpay.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 微信支付配置的必填项与密钥长度校验。
 */
class WechatPayConfigTest {

    @Test
    void requireValidAcceptsCompleteConfig() {
        assertDoesNotThrow(() -> config().requireValid());
    }

    @Test
    void requireValidRejectsMissingPrivateKey() {
        WechatPayConfig config = config();
        config.setPrivateKey(null);
        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, config::requireValid);
        assertTrue(exception.getMessage().contains("privateKey"));
    }

    @Test
    void requireValidRejectsMissingApiV3Key() {
        WechatPayConfig config = config();
        config.setApiV3key(" ");
        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, config::requireValid);
        assertTrue(exception.getMessage().contains("apiV3key"));
    }

    @Test
    void requireValidRejectsApiV3KeyWithWrongLength() {
        WechatPayConfig config = config();
        config.setApiV3key("0123456789abcdef");
        IllegalArgumentException exception =
            assertThrows(IllegalArgumentException.class, config::requireValid);
        assertTrue(exception.getMessage().contains("32位"));
    }

    private WechatPayConfig config() {
        WechatPayConfig config = new WechatPayConfig();
        config.setAppId("wx-app");
        config.setMchId("1900000001");
        config.setSerialNo("SERIAL-1");
        config.setApiV3key("0123456789abcdef0123456789abcdef");
        config.setPrivateKey("-----BEGIN PRIVATE KEY-----");
        return config;
    }
}
