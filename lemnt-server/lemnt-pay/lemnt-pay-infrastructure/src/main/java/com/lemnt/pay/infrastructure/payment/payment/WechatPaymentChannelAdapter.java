package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentChannel;
import com.lemnt.pay.domain.payment.model.PaymentChannelNotification;
import com.lemnt.pay.domain.payment.model.PaymentPrepay;
import com.lemnt.pay.domain.payment.model.PaymentRecord;
import com.lemnt.pay.domain.payment.model.VerifiedPaymentNotification;
import com.lemnt.third.sdk.wechatpay.WechatPayClient;
import com.lemnt.third.sdk.wechatpay.WechatPayNotifyRequest;
import com.wechat.pay.java.service.payments.model.Transaction;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 微信支付渠道：有一次性登录凭证时走小程序 JSAPI，否则退回扫码支付；回调验签解密由官方 SDK 完成。
 */
@Component
@RequiredArgsConstructor
public class WechatPaymentChannelAdapter implements PaymentChannelAdapter {
    private final WechatPayClientProvider clients;

    @Override
    public String channel() {
        return PaymentChannel.WECHAT.name();
    }

    /**
     * 会员已绑定 openid 时按 JSAPI 下单并返回调起支付参数；未绑定时返回 Native 支付二维码，
     * 二维码地址放在 params.codeUrl，不占用 payUrl。
     */
    @Override
    public PaymentPrepay prepay(
        PaymentRecord payment, PayChannelConfig config, String payerOpenId) {
        require(
            payment != null && text(payment.paymentNo()) && text(payment.orderNo()),
            "支付单不合法");
        require(payment.amount() != null && payment.amount().signum() > 0, "支付金额必须大于0");
        WechatPayClient client = clients.get(config);
        require(text(client.payNotifyUrl()), "微信支付配置缺少payNotifyUrl");
        return text(payerOpenId)
            ? WechatJsapiPrepay.create(client, payment, payerOpenId)
            : WechatNativePrepay.create(client, payment);
    }

    /**
     * 验签、解密并核验商户与应用；渠道金额以分返回，按两位小数换算为元。
     */
    @Override
    public VerifiedPaymentNotification verify(
        PayChannelConfig config, PaymentChannelNotification notification) {
        require(notification != null, "支付通知不能为空");
        WechatPayClient client = clients.get(config);
        Transaction transaction =
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
            Transaction.TradeStateEnum.SUCCESS == transaction.getTradeState(),
            "微信支付通知交易未成功");
        require(
            text(client.mchId()) && client.mchId().equals(transaction.getMchid()),
            "微信支付通知商户号不匹配");
        require(
            text(client.appId()) && client.appId().equals(transaction.getAppid()),
            "微信支付通知应用不匹配");
        require(text(transaction.getOutTradeNo()), "微信支付通知缺少商户单号");
        require(text(transaction.getTransactionId()), "微信支付通知缺少渠道交易号");
        require(text(transaction.getAttach()), "微信支付通知缺少订单编号");
        var amount = transaction.getAmount();
        require(
            amount != null && amount.getTotal() != null && amount.getTotal() > 0,
            "微信支付通知金额不合法");
        require(
            WechatPaymentSupport.CURRENCY.equals(amount.getCurrency()),
            "微信支付通知币种不合法");
        return new VerifiedPaymentNotification(
            transaction.getOutTradeNo(),
            transaction.getAttach(),
            channel(),
            transaction.getTransactionId(),
            BigDecimal.valueOf(amount.getTotal())
                .movePointLeft(2)
                .setScale(2, RoundingMode.UNNECESSARY),
            transaction.getTransactionId());
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
