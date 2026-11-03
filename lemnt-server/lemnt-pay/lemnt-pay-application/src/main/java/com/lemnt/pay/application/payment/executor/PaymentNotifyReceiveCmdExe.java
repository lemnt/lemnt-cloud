package com.lemnt.pay.application.payment.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.application.payment.cmd.PaymentNotifyReceiveCmd;
import com.lemnt.pay.domain.payment.gateway.PayAppGateway;
import com.lemnt.pay.domain.payment.gateway.PayChannelConfigGateway;
import com.lemnt.pay.domain.payment.gateway.PaymentChannelGateway;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.payment.model.PayApp;
import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.pay.domain.payment.model.PaymentChannel;
import com.lemnt.pay.domain.payment.model.PaymentRecord;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.Locale;

/** 接收渠道支付通知：先按应用解析渠道配置并验签，再提交支付结果。 */
@Component
@RequiredArgsConstructor
public class PaymentNotifyReceiveCmdExe {
    private final PayAppGateway apps;
    private final PayChannelConfigGateway channels;
    private final TradePaymentGateway payments;
    private final PaymentChannelGateway channelGateway;
    private final PaymentNotifyCmdExe notify;

    public String execute(PaymentNotifyReceiveCmd cmd) {
        require(cmd != null && cmd.appKey() != null && !cmd.appKey().isBlank(), "支付应用不能为空");
        String channel = channel(cmd.channel());
        PayApp app = apps.findByAppKey(cmd.appKey());
        require(app != null && Integer.valueOf(0).equals(app.status()), "支付应用不存在或已停用");
        PayChannelConfig config =
                channels.listByAppId(app.id()).stream()
                        .filter(
                                value ->
                                        channel.equals(value.code())
                                                && Integer.valueOf(0).equals(value.status()))
                        .findFirst()
                        .orElseThrow(() -> new ServiceException("支付渠道未启用", 400));
        var verified =
                channelGateway.verify(
                        channel, config, cmd.body(), cmd.signature(), cmd.timestamp());
        PaymentRecord payment = payments.findByPaymentNo(verified.paymentNo());
        require(
                payment != null
                        && channel.equals(payment.channel())
                        && (payment.appKey() == null || app.appKey().equals(payment.appKey())),
                "支付通知与本地支付单不匹配");
        notify.execute(verified);
        return verified.paymentNo();
    }

    private String channel(String value) {
        try {
            return PaymentChannel.valueOf(
                            value == null ? "" : value.trim().toUpperCase(Locale.ROOT))
                    .name();
        } catch (IllegalArgumentException exception) {
            throw new ServiceException("暂不支持该支付渠道", 400);
        }
    }

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }
}
