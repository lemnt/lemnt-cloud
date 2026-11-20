package com.lemnt.pay.application.payment.executor;

import com.lemnt.pay.application.payment.converter.RechargeDTOConverter;
import com.lemnt.pay.application.payment.dto.WalletRechargeDTO;
import com.lemnt.pay.application.payment.service.RechargeService;
import com.lemnt.pay.application.paymentrefund.service.PaymentRefundService;
import com.lemnt.pay.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.pay.domain.paymentrefund.gateway.PaymentRefundChannelGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 后台充值退款，余额不足时拒绝。
 */
@Component
@RequiredArgsConstructor
public class WalletRechargeRefundCmdExe {
    private final RechargeService service;
    private final TradePaymentGateway payments;
    private final PaymentRefundChannelGateway channels;
    private final PaymentRefundService results;

    public WalletRechargeDTO execute(String rechargeNo) {
        var refund = service.prepareChannelRefund(rechargeNo);
        if (refund == null) return RechargeDTOConverter.recharge(service.refund(rechargeNo));
        if ("WAITING".equals(refund.status())) {
            var payment = payments.findByPaymentNo(refund.paymentNo());
            results.complete(refund, channels.refund(refund, payment));
        }
        return RechargeDTOConverter.recharge(service.find(rechargeNo));
    }
}
