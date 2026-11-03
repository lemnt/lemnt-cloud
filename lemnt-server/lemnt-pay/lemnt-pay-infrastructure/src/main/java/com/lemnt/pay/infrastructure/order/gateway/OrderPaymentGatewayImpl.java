package com.lemnt.pay.infrastructure.order.gateway;

import com.lemnt.order.client.OrderSnapshotClient;
import com.lemnt.order.client.dto.FrozenRefundDTO;
import com.lemnt.pay.domain.order.gateway.OrderPaymentGateway;
import com.lemnt.pay.domain.order.model.FrozenRefundPlan;
import com.lemnt.pay.domain.order.model.OrderPaymentSnapshot;
import com.lemnt.pay.domain.payment.model.PaymentOperator;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 所有订单校验均通过Client；RPC超时保留原业务号重试，不伪造默认快照。 */
@Component
public class OrderPaymentGatewayImpl implements OrderPaymentGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private OrderSnapshotClient client;

    @Override
    public OrderPaymentSnapshot preparePayment(String no) {
        var value=client.preparePayment(no);
        return value==null?null:new OrderPaymentSnapshot(value.orderNo(),value.memberId(),value.totalAmount(),value.status());
    }

    @Override
    public OrderPaymentSnapshot findByOrderNo(String no) {
        var value = client.paymentOrder(no);
        return value == null
                ? null
                : new OrderPaymentSnapshot(
                        value.orderNo(), value.memberId(), value.totalAmount(), value.status());
    }

    @Override
    public FrozenRefundPlan prepareRefund(String no, PaymentOperator operator) {
        if (operator == null || (operator.userType() != 0 && operator.userType() != 2))
            throw new IllegalArgumentException("退款发起方不合法");
        return from(client.prepareRefund(no, operator.userType() == 0 ? null : operator.userId()));
    }

    @Override
    public FrozenRefundPlan refundPlan(String no) {
        return from(client.refundPlan(no));
    }

    private FrozenRefundPlan from(FrozenRefundDTO value) {
        return value == null
                ? null
                : new FrozenRefundPlan(
                        value.afterSaleNo(),
                        value.orderNo(),
                        value.memberId(),
                        value.paymentNo(),
                        value.amount(),
                        value.ruleVersion(),
                        value.status());
    }
}
