package com.lemnt.trade.application.paymentrefund.executor;

import com.lemnt.trade.application.paymentrefund.converter.PaymentRefundDTOConverter;
import com.lemnt.trade.application.paymentrefund.dto.PaymentRefundDTO;
import com.lemnt.trade.application.paymentrefund.service.PaymentRefundService;
import com.lemnt.trade.application.paymentrefund.service.RefundBenefitsService;
import com.lemnt.trade.domain.order.model.TradeOperator;
import com.lemnt.trade.domain.payment.gateway.TradePaymentGateway;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundChannelGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 售后发起原路退款，退款单与渠道结果独立提交。 */
@Component
@RequiredArgsConstructor
public class PaymentRefundCreateCmdExe {
    private final PaymentRefundService service;
    private final RefundBenefitsService benefits;
    private final TradePaymentGateway payments;
    private final PaymentRefundChannelGateway channels;

    /** 单售后编号入参，退款金额和渠道全部取自服务端原单。 */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentRefundDTO execute(String afterSaleNo) {
        return execute(afterSaleNo, TradeOperator.system());
    }

    /** 后台发起原路退款时保留实际操作者。 */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentRefundDTO executeForAdmin(String afterSaleNo, Long operatorId) {
        return execute(afterSaleNo, TradeOperator.administrator(operatorId));
    }

    private PaymentRefundDTO execute(String afterSaleNo, TradeOperator operator) {
        var refund = service.prepare(afterSaleNo, operator);
        if ("SUCCESS".equals(refund.status())) {
            benefits.complete(refund);
            return PaymentRefundDTOConverter.toDTO(refund);
        }
        var payment = payments.findByPaymentNo(refund.paymentNo());
        if (payment == null || !"SUCCESS".equals(payment.status())
                || !payment.orderNo().equals(refund.orderNo()) || !payment.channel().equals(refund.channel())
                || refund.amount().signum() <= 0 || payment.amount().compareTo(refund.amount()) < 0) {
            throw new IllegalStateException("原支付单与退款交易不一致，请先对账");
        }
        var result = service.complete(refund, channels.refund(refund, payment));
        benefits.complete(result);
        return PaymentRefundDTOConverter.toDTO(result);
    }
}
