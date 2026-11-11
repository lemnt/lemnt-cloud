package com.lemnt.order.application.paymentrefund.executor;

import com.lemnt.order.application.aftersale.service.AfterSaleRefundCoordinator;
import com.lemnt.order.application.paymentrefund.dto.PaymentRefundDTO;
import com.lemnt.order.domain.order.model.TradeOperator;
import com.lemnt.order.domain.payment.gateway.PaymentGateway;
import com.lemnt.order.domain.paymentrefund.gateway.PaymentRefundGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 售后先提交原计划，再在订单事务外请求现金退款；完成由资金事件核对驱动。
 */
@Component
@RequiredArgsConstructor
public class PaymentRefundCreateCmdExe {
    private final AfterSaleRefundCoordinator coordinator;
    private final PaymentGateway payments;
    private final PaymentRefundGateway refunds;

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentRefundDTO execute(String no) {
        return execute(no, TradeOperator.system());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public PaymentRefundDTO executeForAdmin(String no, Long operator) {
        return execute(no, TradeOperator.administrator(operator));
    }

    private PaymentRefundDTO execute(String no, TradeOperator operator) {
        var af = coordinator.prepare(no, operator);
        if (af.amount().signum() == 0)
            return new PaymentRefundDTO(
                null, no, af.paymentNo(), af.orderNo(), "NONE", af.amount(), "SUCCESS", null);
        var value =
            "REFUNDED".equals(af.status())
                ? refunds.findByAfterSaleNo(no)
                : payments.refund(af);
        if (value == null) throw new IllegalStateException("原成功退款记录缺失，请先对账");
        if ("REFUNDED".equals(af.status()) && !"SUCCESS".equals(value.status()))
            throw new IllegalStateException("原成功退款与售后状态不一致，请先对账");
        // 网络重试时查询到真实成功结果，也走同一冻结方案核验及本地事务；MQ迟到仍然幂等。
        if ("SUCCESS".equals(value.status())) coordinator.cashSucceeded(value);
        if ("FAILED".equals(value.status())) coordinator.cashFailed(value, operator);
        return new PaymentRefundDTO(
            value.refundNo(),
            value.afterSaleNo(),
            value.paymentNo(),
            value.orderNo(),
            value.channel(),
            value.amount(),
            value.status(),
            value.channelRefundNo());
    }
}
