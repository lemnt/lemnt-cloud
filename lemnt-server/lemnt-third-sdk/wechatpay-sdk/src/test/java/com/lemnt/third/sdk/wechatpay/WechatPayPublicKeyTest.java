package com.lemnt.third.sdk.wechatpay;

import com.lemnt.third.sdk.wechatpay.config.WechatPayConfig;

import com.wechat.pay.java.core.exception.ValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WechatPayPublicKeyTest {
    private static final String PUBLIC_KEY_ID = "PUB_KEY_ID_TEST";
    private static final String API_V3_KEY = "0123456789abcdef0123456789abcdef";

    /** 使用本地测试密钥生成渠道报文，覆盖支付、退款和错误公钥 ID。 */
    @Test
    void verifiesPaymentAndRefundWithConfiguredWechatPublicKey() throws Exception {
        var generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        var merchant = generator.generateKeyPair();
        var wechat = generator.generateKeyPair();
        var config = new WechatPayConfig();
        config.setAppId("wx-app");
        config.setMchId("1900000001");
        config.setSerialNo("MERCHANT_SERIAL");
        config.setPrivateKey(pem("PRIVATE KEY", merchant.getPrivate().getEncoded()));
        config.setPublicKey(pem("PUBLIC KEY", wechat.getPublic().getEncoded()));
        config.setPublicKeyId(PUBLIC_KEY_ID);
        config.setApiV3key(API_V3_KEY);
        var client = WechatPayClient.of(config);

        var payBody =
                """
                {"out_trade_no":"P1","attach":"O1","transaction_id":"WX1","mchid":"1900000001",
                 "appid":"wx-app","trade_state":"SUCCESS","amount":{"currency":"CNY","total":800}}
                """;
        var payment =
                client.parseTransactionNotification(
                        notification(wechat.getPrivate(), PUBLIC_KEY_ID, payBody));
        assertEquals("P1", payment.outTradeNo());
        assertEquals(800, payment.totalFen());

        var refundBody =
                """
                {"out_refund_no":"R1","out_trade_no":"P1","refund_id":"WR1",
                 "refund_status":"SUCCESS","amount":{"refund":250}}
                """;
        var refund =
                client.parseRefundNotification(
                        notification(wechat.getPrivate(), PUBLIC_KEY_ID, refundBody));
        assertEquals("R1", refund.outRefundNo());
        assertEquals(new BigDecimal("2.50"), refund.refundAmount());

        var wrongId = notification(wechat.getPrivate(), "PUB_KEY_ID_OTHER", refundBody);
        assertThrows(ValidationException.class, () -> client.parseRefundNotification(wrongId));
    }

    private WechatPayNotifyRequest notification(PrivateKey signer, String keyId, String payload)
            throws Exception {
        String now = Long.toString(Instant.now().getEpochSecond());
        String nonce = "123456789012";
        String associatedData = "test-notification";
        var cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(
                Cipher.ENCRYPT_MODE,
                new SecretKeySpec(API_V3_KEY.getBytes(UTF_8), "AES"),
                new GCMParameterSpec(128, nonce.getBytes(UTF_8)));
        cipher.updateAAD(associatedData.getBytes(UTF_8));
        String ciphertext =
                Base64.getEncoder().encodeToString(cipher.doFinal(payload.getBytes(UTF_8)));
        String body =
                """
                {"resource":{"algorithm":"AEAD_AES_256_GCM","ciphertext":"%s",
                 "nonce":"%s","associated_data":"%s"}}
                """
                        .formatted(ciphertext, nonce, associatedData);
        var signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(signer);
        signature.update((now + "\n" + nonce + "\n" + body + "\n").getBytes(UTF_8));
        return new WechatPayNotifyRequest(
                keyId,
                Base64.getEncoder().encodeToString(signature.sign()),
                now,
                nonce,
                null,
                body);
    }

    private String pem(String type, byte[] bytes) {
        return "-----BEGIN "
                + type
                + "-----\n"
                + Base64.getMimeEncoder(64, new byte[] {'\n'}).encodeToString(bytes)
                + "\n-----END "
                + type
                + "-----";
    }
}
