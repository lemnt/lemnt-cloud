package com.lemnt.member.start;

import com.lemnt.member.infrastructure.account.gateway.*;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberProviderConfigurationTest {
    @Test
    void missingProviderCredentialsFailClosedWithoutNetwork() {
        var redis = mock(StringRedisTemplate.class);
        var wx = new WxMemberSocialGateway(redis, "", "", "", "", "", "");
        assertThrows(UnsupportedOperationException.class, () -> wx.namespace("WECHAT_MINI"));
        assertThrows(
                UnsupportedOperationException.class, () -> wx.login("WECHAT_MP", "code", null));
        assertThrows(UnsupportedOperationException.class, () -> wx.namespace("QQ"));
        verifyNoInteractions(redis);
        var sms = new AliyunMemberSmsDeliveryGateway("", "", "", "", "", 100);
        assertFalse(sms.configured());
        assertThrows(
                UnsupportedOperationException.class,
                () -> sms.hash("13800000001", "LOGIN", "123456"));
        assertThrows(UnsupportedOperationException.class, () -> sms.send("13800000001", "123456"));
    }

    @Test
    void hmacBindsPhoneSceneCodeAndPepper() {
        var a =
                new AliyunMemberSmsDeliveryGateway(
                        "test", "test", "test", "test", "a".repeat(32), 100);
        var b =
                new AliyunMemberSmsDeliveryGateway(
                        "test", "test", "test", "test", "b".repeat(32), 100);
        var hash = a.hash("13800000001", "LOGIN", "123456");
        assertEquals(64, hash.length());
        assertFalse(hash.contains("123456"));
        assertNotEquals(hash, a.hash("13800000002", "LOGIN", "123456"));
        assertNotEquals(hash, a.hash("13800000001", "RESET_PASSWORD", "123456"));
        assertNotEquals(hash, b.hash("13800000001", "LOGIN", "123456"));
        assertThrows(IllegalArgumentException.class, () -> a.send("+12025550123", "123456"));
    }

    @Test
    void configuredWechatRejectsUnapprovedJsapiUrlBeforeRedisOrNetwork() {
        var redis = mock(StringRedisTemplate.class);
        var wx =
                new WxMemberSocialGateway(
                        redis,
                        "mini-test",
                        "secret",
                        "mp-test",
                        "secret",
                        "https://allowed.example/callback",
                        "allowed.example");
        assertEquals("WECHAT_MINI:mini-test", wx.namespace("WECHAT_MINI"));
        for (String url :
                new String[] {
                    "http://allowed.example/",
                    "https://evil.example/",
                    "https://allowed.example/#fragment",
                    "https://user@allowed.example/"
                }) assertThrows(IllegalArgumentException.class, () -> wx.jsapiSignature(url));
        assertThrows(
                IllegalArgumentException.class,
                () -> wx.qrcode("bad & scene", "pages/index/index"));
        verifyNoInteractions(redis);
    }
}
