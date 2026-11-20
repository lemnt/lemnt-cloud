package com.lemnt.pay.infrastructure.channel.adapter.sandbox;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.pay.domain.channel.model.PayChannelConfig;
import com.lemnt.pay.domain.channel.model.PaymentChannel;
import com.lemnt.pay.domain.channel.model.PaymentChannelNotification;
import com.lemnt.pay.domain.payment.model.PaymentPrepay;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefund;
import com.lemnt.pay.domain.paymentrefund.model.PaymentRefundResult;
import com.lemnt.pay.infrastructure.channel.adapter.AbstractChannelAdapter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * 沙箱渠道：支付与退款都在本地确认，支付通知用 HMAC 签名校验；
 * 仅在显式开启时注册，生产环境不接入。
 */
@Component
@ConditionalOnProperty(prefix = "payment.sandbox", name = "enabled", havingValue = "true")
public class SandboxChannelAdapter implements AbstractChannelAdapter {
    private static final long MAX_SKEW_MILLIS = 5 * 60_000L;
    private static final int MIN_SECRET_LENGTH = 16;
    private static final int MAX_NO_LENGTH = 128;
    private static final String REFUND_NO_PREFIX = "SBR-";

    @Override
    public String channel() {
        return PaymentChannel.SANDBOX.name();
    }

    /**
     * 沙箱不发起真实跳转，只返回客户端确认支付所需的本地参数。
     */
    @Override
    public PaymentPrepay payment(
        PaymentRecord payment, PayChannelConfig config, String payerOpenId) {
        requireConfig(config);
        String secret = secret(config);
        require(secret.length() >= MIN_SECRET_LENGTH, "沙箱渠道密钥至少16位");
        return new PaymentPrepay(
            payment.paymentNo(),
            channel(),
            null,
            Map.of(
                "paymentNo",
                payment.paymentNo(),
                "channel",
                channel(),
                "appKey",
                payment.appKey() == null ? "" : payment.appKey()));
    }

    @Override
    public VerifiedPaymentNotification paymentCallback(
        PayChannelConfig config, PaymentChannelNotification notification) {
        requireConfig(config);
        require(notification != null, "支付通知不能为空");
        String body = notification.body();
        String signature = notification.signature();
        String timestamp = notification.timestamp();
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
        String expected =
            sign(
                secret(config),
                canonical(paymentNo, orderNo, amount, channelTradeNo, notifyId, timestamp));
        require(
            MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.US_ASCII),
                signature.toLowerCase().getBytes(StandardCharsets.US_ASCII)),
            "支付通知签名不合法");
        return new VerifiedPaymentNotification(
            paymentNo, orderNo, channel(), channelTradeNo, amount, notifyId);
    }

    /**
     * 沙箱没有远端渠道交易，关单时直接视为已撤销。
     */
    @Override
    public boolean close(PaymentRecord payment, PayChannelConfig config) {
        return PaymentChannel.SANDBOX.name().equals(payment.channel());
    }

    /**
     * 原退款编号生成稳定的渠道退款编号，重复调用得到同一结果。
     */
    @Override
    public PaymentRefundResult refund(
        PaymentRefund refund, PaymentRecord payment, PayChannelConfig config) {
        requireRefund(refund, payment);
        String channelRefundNo = REFUND_NO_PREFIX + refund.refundNo();
        if (channelRefundNo.length() > MAX_NO_LENGTH) {
            throw new IllegalStateException("沙箱渠道退款编号过长");
        }
        return new PaymentRefundResult(
            refund.refundNo(),
            refund.paymentNo(),
            refund.channel(),
            refund.amount(),
            "SUCCESS",
            channelRefundNo);
    }

    /**
     * 沙箱没有远端退款单，返回空表示本地尚无渠道结果。
     */
    @Override
    public PaymentRefundResult refundQuery(
        PaymentRefund refund, PaymentRecord payment, PayChannelConfig config) {
        requireRefund(refund, payment);
        return null;
    }

    /**
     * 通知签名原文，渠道重放时必须按同一顺序和金额格式计算。
     */
    static String canonical(
        String paymentNo,
        String orderNo,
        BigDecimal amount,
        String channelTradeNo,
        String notifyId,
        String timestamp) {
        return String.join(
            "\n",
            PaymentChannel.SANDBOX.name(),
            paymentNo,
            orderNo,
            amount.toPlainString(),
            channelTradeNo,
            notifyId,
            timestamp);
    }

    static String sign(String secret, String canonical) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of()
                .formatHex(mac.doFinal(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("沙箱支付签名失败", exception);
        }
    }

    private void requireConfig(PayChannelConfig config) {
        require(
            config != null
                && PaymentChannel.SANDBOX.name().equals(config.code())
                && Integer.valueOf(0).equals(config.status()),
            "沙箱支付渠道未启用");
    }

    private String secret(PayChannelConfig config) {
        Map<String, Object> value = JsonUtils.toMap(config.config());
        require(
            value != null
                && value.get("hmacSecret") instanceof String
                && !((String) value.get("hmacSecret")).isBlank(),
            "沙箱渠道缺少hmacSecret");
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
        require(
            value instanceof String text && !text.isBlank() && text.length() <= MAX_NO_LENGTH,
            "支付通知" + key + "不合法");
        return (String) value;
    }

    private BigDecimal amount(Object value) {
        try {
            BigDecimal amount =
                new BigDecimal(String.valueOf(value)).setScale(2, RoundingMode.UNNECESSARY);
            require(amount.signum() > 0, "支付通知金额不合法");
            return amount;
        } catch (RuntimeException exception) {
            throw new ServiceException("支付通知金额不合法", 400);
        }
    }

    /**
     * 退款单必须对应本人成功的原支付，金额不超过原支付。
     */
    private void requireRefund(PaymentRefund refund, PaymentRecord payment) {
        if (refund == null
            || payment == null
            || !PaymentChannel.SANDBOX.name().equals(refund.channel())
            || !PaymentChannel.SANDBOX.name().equals(payment.channel())
            || refund.paymentNo() == null
            || !refund.paymentNo().equals(payment.paymentNo())
            || refund.amount() == null
            || payment.amount() == null
            || refund.amount().compareTo(payment.amount()) > 0
            || !"SUCCESS".equals(payment.status())
            || payment.channelTradeNo() == null
            || payment.channelTradeNo().isBlank()) {
            throw new IllegalStateException("沙箱退款与原支付不一致");
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }
}
