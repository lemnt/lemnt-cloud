package com.lemnt.pay.application.payment.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.pay.application.payment.dto.PaymentPrepayDTO;
import com.lemnt.pay.application.payment.service.PaymentAttemptService;
import com.lemnt.pay.application.payment.service.RechargeService;
import com.lemnt.pay.domain.channel.gateway.PayAppGateway;
import com.lemnt.pay.domain.channel.gateway.PayChannelConfigGateway;
import com.lemnt.pay.domain.channel.gateway.PaymentChannelGateway;
import com.lemnt.pay.domain.payment.gateway.PayerOpenIdGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 为本人充值单生成微信小程序支付参数，实付金额取原套餐快照。 */
@Component
@RequiredArgsConstructor
public class WalletRechargePrepayCmdExe {
    private final RechargeService recharges;
    private final PayAppGateway apps;
    private final PayChannelConfigGateway configurations;
    private final PaymentChannelGateway channels;
    private final PayerOpenIdGateway openIds;
    private final PaymentAttemptService attempts;

    public PaymentPrepayDTO execute(Long memberId, String rechargeNo, String appKey) {
        var app = apps.findByAppKey(appKey);
        if (app == null
                || !Integer.valueOf(0).equals(app.status())
                || !channels.supports("WECHAT")) {
            throw new ServiceException("微信充值应用未启用", 400);
        }
        var config =
                configurations.listByAppId(app.id()).stream()
                        .filter(
                                item ->
                                        "WECHAT".equals(item.code())
                                                && Integer.valueOf(0).equals(item.status()))
                        .findFirst()
                        .orElseThrow(() -> new ServiceException("微信充值渠道未启用", 400));
        String openId = openIds.findOpenId(memberId);
        if (openId == null || openId.isBlank()) throw new ServiceException("请先完成微信登录", 400);
        var payment = recharges.preparePayment(memberId, rechargeNo, app.appKey());
        var prepay = attempts.prepay(payment, config, openId);
        return new PaymentPrepayDTO(
                payment.paymentNo(),
                payment.orderNo(),
                payment.channel(),
                payment.amount(),
                prepay.payUrl(),
                prepay.params());
    }
}
