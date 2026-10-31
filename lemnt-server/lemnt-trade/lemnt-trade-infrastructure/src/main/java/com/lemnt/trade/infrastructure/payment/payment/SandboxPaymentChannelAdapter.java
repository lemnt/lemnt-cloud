package com.lemnt.trade.infrastructure.payment.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.trade.domain.payment.model.PayChannelConfig;
import com.lemnt.trade.domain.payment.model.PaymentChannel;
import com.lemnt.trade.domain.payment.model.PaymentPrepay;
import com.lemnt.trade.domain.payment.model.PaymentRecord;
import com.lemnt.trade.domain.payment.model.VerifiedPaymentNotification;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;

/** 沙箱渠道：本地 HMAC 签名验证支付通知，仅在显式开启时注册，生产环境不接入。 */
@Component
@ConditionalOnProperty(prefix = "payment.sandbox", name = "enabled", havingValue = "true")
public class SandboxPaymentChannelAdapter implements PaymentChannelAdapter {
    private static final long MAX_SKEW_MILLIS = 5 * 60_000L;
    private static final int MIN_SECRET_LENGTH = 16;
    private static final int MAX_NO_LENGTH = 128;

    @Override
    public String channel() {
        return PaymentChannel.SANDBOX.name();
    }

    /** 沙箱不发起真实跳转，只返回客户端确认支付所需的本地参数。 */
    @Override
    public PaymentPrepay prepay(PaymentRecord payment, PayChannelConfig config) {
        requireConfig(config);
        String secret = secret(config);
        require(secret.length() >= MIN_SECRET_LENGTH, "沙箱渠道密钥至少16位");
        return new PaymentPrepay(payment.paymentNo(), channel(), null,
                Map.of("paymentNo", payment.paymentNo(), "channel", channel(),
                        "appKey", payment.appKey() == null ? "" : payment.appKey()));
    }

    @Override
    public VerifiedPaymentNotification verify(PayChannelConfig config, String body, String signature,
            String timestamp) {
        requireConfig(config);
        require(body != null && !body.isBlank(), "支付通知不能为空");
        require(signature != null && !signature.isBlank(), "支付通知签名不能为空");
        long time = time(timestamp);
        long now = System.currentTimeMillis();
        require(Math.abs(now - time) <= MAX_SKEW_MILLIS, "支付通知时间戳超出允许范围");
        Map<String, Object> payload = payload(body);
        String paymentNo = text(payload, "paymentNo");
        String orderNo = text(payload, "orderNo");
        String channelTradeNo = text(payload, "channelTradeNo");
        String notifyId = text(payload, "notifyId");
        BigDecimal amount = amount(payload.get("amount"));
        String expected = sign(secret(config), canonical(paymentNo, orderNo, amount, channelTradeNo,
                notifyId, timestamp));
        require(MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                signature.toLowerCase().getBytes(StandardCharsets.US_ASCII)), "支付通知签名不合法");
        return new VerifiedPaymentNotification(paymentNo, orderNo, channel(), channelTradeNo, amount,
                notifyId);
    }

    /** 通知签名原文，渠道重放时必须按同一顺序和金额格式计算。 */
    static String canonical(String paymentNo, String orderNo, BigDecimal amount, String channelTradeNo,
            String notifyId, String timestamp) {
        return String.join("\n", PaymentChannel.SANDBOX.name(), paymentNo, orderNo,
                amount.toPlainString(), channelTradeNo, notifyId, timestamp);
    }

    static String sign(String secret, String canonical) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("沙箱支付签名失败", exception);
        }
    }

    private void requireConfig(PayChannelConfig config) {
        require(config != null && PaymentChannel.SANDBOX.name().equals(config.code())
                && Integer.valueOf(0).equals(config.status()), "沙箱支付渠道未启用");
    }

    private String secret(PayChannelConfig config) {
        Map<String, Object> value = JsonUtils.toMap(config.config());
        require(value != null && value.get("hmacSecret") instanceof String
                && !((String) value.get("hmacSecret")).isBlank(), "沙箱渠道缺少hmacSecret");
        return (String) value.get("hmacSecret");
    }

    private Map<String, Object> payload(String body) {
        try {
            Map<String, Object> value = JsonUtils.toMap(body);
            require(value != null, "支付通知不是合法JSON");
            return value;
        } catch (RuntimeException exception) {
            throw new ServiceException("支付通知不是合法JSON", 400);
        }
    }

    private long time(String timestamp) {
        try {
            return Long.parseLong(timestamp);
        } catch (RuntimeException exception) {
            throw new ServiceException("支付通知时间戳不合法", 400);
        }
    }

    private String text(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        require(value instanceof String text && !text.isBlank() && text.length() <= MAX_NO_LENGTH,
                "支付通知" + key + "不合法");
        return (String) value;
    }

    private BigDecimal amount(Object value) {
        try {
            BigDecimal amount = new BigDecimal(String.valueOf(value)).setScale(2, RoundingMode.UNNECESSARY);
            require(amount.signum() > 0, "支付通知金额不合法");
            return amount;
        } catch (RuntimeException exception) {
            throw new ServiceException("支付通知金额不合法", 400);
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }
}
