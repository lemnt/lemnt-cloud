package com.lemnt.pay.infrastructure.channel.adapter.wechatpay;

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
import com.lemnt.third.sdk.wechatpay.WechatPayClient;
import com.lemnt.third.sdk.wechatpay.WechatPayNotifyRequest;
import com.lemnt.third.sdk.wechatpay.WechatPrepayCommand;
import com.lemnt.third.sdk.wechatpay.WechatRefundCommand;
import com.lemnt.third.sdk.wechatpay.WechatRefundResult;
import com.lemnt.third.sdk.wechatpay.WechatRefundStatus;
import com.lemnt.third.sdk.wechatpay.WechatTransaction;
import com.lemnt.third.sdk.wechatpay.config.WechatPayConfig;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信支付渠道：会员绑定 openid 时走小程序 JSAPI，否则退回扫码支付；
 * 微信参数、签名与验签细节由 wechatpay-sdk 承担，本类只做业务单与渠道事实的映射和核验。
 */
@Component
public class WechatChannelAdapter implements AbstractChannelAdapter {
    private static final String CURRENCY = "CNY";
    private static final String TRADE_STATE_SUCCESS = "SUCCESS";

    /**
     * 官方 SDK 创建客户端时会下载平台证书，按配置内容缓存，避免每次请求重建。
     */
    private final Map<String, WechatPayClient> clientCache = new ConcurrentHashMap<>();

    @Override
    public String channel() {
        return PaymentChannel.WECHAT.name();
    }

    /**
     * 取渠道配置对应的 SDK 客户端；配置内容变化时重建，避免沿用旧商户号或旧私钥。
     */
    WechatPayClient client(PayChannelConfig config) {
        if (config == null || config.config() == null || config.config().isBlank()) {
            throw new ServiceException("微信支付渠道配置不能为空", 400);
        }
        String key = config.id() + ":" + config.config();
        return clientCache.computeIfAbsent(key, ignored -> create(config.config()));
    }

    private WechatPayClient create(String value) {
        WechatPayConfig channelConfig = JsonUtils.toBean(value, WechatPayConfig.class);
        if (channelConfig == null) {
            throw new ServiceException("微信支付渠道配置不是合法JSON", 400);
        }
        try {
            return WechatPayClient.of(channelConfig);
        } catch (IllegalArgumentException exception) {
            throw new ServiceException(exception.getMessage(), 400);
        }
    }

    @Override
    public PaymentPrepay payment(
        PaymentRecord payment, PayChannelConfig config, String payerOpenId) {
        require(
            payment != null && text(payment.paymentNo()) && text(payment.orderNo()),
            "支付单不合法");
        require(payment.amount() != null && payment.amount().signum() > 0, "支付金额必须大于0");
        WechatPayClient client = client(config);
        require(text(client.payNotifyUrl()), "微信支付配置缺少payNotifyUrl");
        var result =
            client.prepay(
                new WechatPrepayCommand(
                    payment.paymentNo(),
                    payment.orderNo(),
                    payment.amount(),
                    client.payNotifyUrl(),
                    payerOpenId));
        return new PaymentPrepay(payment.paymentNo(), channel(), null, result.params());
    }

    @Override
    public VerifiedPaymentNotification paymentCallback(
        PayChannelConfig config, PaymentChannelNotification notification) {
        require(notification != null, "支付通知不能为空");
        WechatPayClient client = client(config);
        WechatTransaction transaction =
            client.parseTransactionNotification(
                new WechatPayNotifyRequest(
                    notification.serialNumber(),
                    notification.signature(),
                    notification.timestamp(),
                    notification.nonce(),
                    notification.signType(),
                    notification.body()));
        require(transaction != null, "微信支付通知缺少交易信息");
        require(
            TRADE_STATE_SUCCESS.equals(transaction.tradeState()),
            "微信支付通知交易未成功");
        require(
            text(client.mchId()) && client.mchId().equals(transaction.mchId()),
            "微信支付通知商户号不匹配");
        require(
            text(client.appId()) && client.appId().equals(transaction.appId()),
            "微信支付通知应用不匹配");
        require(text(transaction.outTradeNo()), "微信支付通知缺少商户单号");
        require(text(transaction.transactionId()), "微信支付通知缺少渠道交易号");
        require(text(transaction.orderNo()), "微信支付通知缺少订单编号");
        require(
            transaction.totalFen() != null && transaction.totalFen() > 0,
            "微信支付通知金额不合法");
        require(CURRENCY.equals(transaction.currency()), "微信支付通知币种不合法");
        return new VerifiedPaymentNotification(
            transaction.outTradeNo(),
            transaction.orderNo(),
            channel(),
            transaction.transactionId(),
            BigDecimal.valueOf(transaction.totalFen())
                .movePointLeft(2)
                .setScale(2, RoundingMode.UNNECESSARY),
            transaction.transactionId());
    }

    @Override
    public boolean close(PaymentRecord payment, PayChannelConfig config) {
        return client(config).closeOrder(payment.paymentNo());
    }

    @Override
    public PaymentRefundResult refund(
        PaymentRefund refund, PaymentRecord payment, PayChannelConfig config) {
        requireRefund(refund, payment);
        WechatPayClient client = client(config);
        WechatRefundResult result =
            client.refund(
                new WechatRefundCommand(
                    refund.refundNo(),
                    payment.paymentNo(),
                    refund.amount(),
                    payment.amount(),
                    "售后" + refund.afterSaleNo()));
        return toRefundResult(result, refund, payment);
    }

    @Override
    public PaymentRefundResult refundQuery(
        PaymentRefund refund, PaymentRecord payment, PayChannelConfig config) {
        requireRefund(refund, payment);
        WechatRefundResult result = client(config).queryRefund(refund.refundNo());
        return result == null ? null : toRefundResult(result, refund, payment);
    }

    /**
     * 核对渠道退款单号、原支付单号与金额后映射为本地退款结果。
     */
    private PaymentRefundResult toRefundResult(
        WechatRefundResult value, PaymentRefund refund, PaymentRecord payment) {
        if (!refund.refundNo().equals(value.outRefundNo())) {
            throw new IllegalStateException("微信退款单号与本地退款单不一致");
        }
        if (value.outTradeNo() != null && !payment.paymentNo().equals(value.outTradeNo())) {
            throw new IllegalStateException("微信退款绑定的原支付单不一致");
        }
        if (value.refundAmount() == null || value.refundAmount().compareTo(refund.amount()) != 0) {
            throw new IllegalStateException("微信退款金额与本地退款单不一致");
        }
        return new PaymentRefundResult(
            refund.refundNo(),
            refund.paymentNo(),
            channel(),
            value.refundAmount(),
            refundStatus(value.status()),
            value.refundId());
    }

    /**
     * 渠道退款状态映射到本地资金状态；异常单需人工核对，不当作成功。
     */
    private String refundStatus(WechatRefundStatus status) {
        return switch (status) {
            case SUCCESS -> "SUCCESS";
            case PROCESSING -> "WAITING";
            case FAILED -> "FAILED";
        };
    }

    /**
     * 退款单必须对应本人成功的原支付，金额不超过原支付。
     */
    private void requireRefund(PaymentRefund refund, PaymentRecord payment) {
        if (refund == null
            || payment == null
            || !PaymentChannel.WECHAT.name().equals(refund.channel())
            || !PaymentChannel.WECHAT.name().equals(payment.channel())
            || refund.paymentNo() == null
            || !refund.paymentNo().equals(payment.paymentNo())
            || refund.amount() == null
            || payment.amount() == null
            || refund.amount().compareTo(payment.amount()) > 0
            || !"SUCCESS".equals(payment.status())
            || payment.channelTradeNo() == null
            || payment.channelTradeNo().isBlank()) {
            throw new IllegalStateException("微信退款与原支付不一致");
        }
    }

    private boolean text(String value) {
        return value != null && !value.isBlank();
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }
}
