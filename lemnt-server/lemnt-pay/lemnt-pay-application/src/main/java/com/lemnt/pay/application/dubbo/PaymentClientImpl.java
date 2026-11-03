package com.lemnt.pay.application.dubbo;

import com.lemnt.pay.application.payment.cmd.PaymentPrepayCmd;
import com.lemnt.pay.application.payment.executor.PaymentPrepayCmdExe;
import com.lemnt.pay.application.paymentrefund.executor.PaymentRefundCreateCmdExe;
import com.lemnt.pay.client.PaymentClient;
import com.lemnt.pay.client.bo.PaymentPrepayBo;
import com.lemnt.pay.client.bo.PaymentRefundBo;
import com.lemnt.pay.client.dto.PaymentPrepayResultDTO;
import com.lemnt.pay.client.dto.PaymentRefundResultDTO;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

/** 私有资金操作入口，金额与权益由原单决定，不能由调用方覆盖。 */
@DubboService
@RequiredArgsConstructor
public class PaymentClientImpl implements PaymentClient {
    private final PaymentPrepayCmdExe prepay;
    private final PaymentRefundCreateCmdExe refunds;
    private final OrderPaymentGateway orders;

    @Override
    public PaymentPrepayResultDTO prepay(PaymentPrepayBo input) {
        if (input == null) throw new IllegalArgumentException("预支付请求不能为空");
        var result =
                prepay.execute(
                        new PaymentPrepayCmd(
                                input.memberId(),
                                input.orderNo(),
                                input.appKey(),
                                input.channel()));
        return new PaymentPrepayResultDTO(
                result.paymentNo(),
                result.orderNo(),
                result.channel(),
                result.amount(),
                result.payUrl(),
                result.params());
    }

    @Override
    public PaymentRefundResultDTO refund(PaymentRefundBo input) {
        if (input == null || input.amount() == null) throw new IllegalArgumentException("退款请求不能为空");
        var frozen = orders.refundPlan(input.afterSaleNo());
        if (frozen == null
                || !Objects.equals(frozen.orderNo(), input.orderNo())
                || !Objects.equals(frozen.paymentNo(), input.paymentNo())
                || !Objects.equals(frozen.memberId(), input.memberId())
                || frozen.amount().compareTo(input.amount()) != 0
                || !Objects.equals(frozen.ruleVersion(), input.ruleVersion()))
            throw new IllegalStateException("退款请求与原售后冻结方案不一致");
        var result = refunds.execute(input.afterSaleNo());
        return new PaymentRefundResultDTO(
                result.refundNo(),
                result.afterSaleNo(),
                result.paymentNo(),
                result.orderNo(),
                result.channel(),
                result.amount(),
                result.status(),
                result.channelRefundNo());
    }
}
