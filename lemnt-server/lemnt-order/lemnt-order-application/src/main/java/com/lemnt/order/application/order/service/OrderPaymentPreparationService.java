package com.lemnt.order.application.order.service;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.domain.event.gateway.MessageInboxGateway;
import com.lemnt.order.domain.order.gateway.TradeOrderGateway;
import com.lemnt.order.domain.order.model.TradeOrder;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;

/**
 * 支付准备先固定原金额，RPC超时也不能允许后台调价改变渠道应付。
 */
@Component
@RequiredArgsConstructor
public class OrderPaymentPreparationService {
    private final OrderPersistenceService orders;
    private final MessageInboxGateway inbox;
    private final TradeOrderGateway paymentFacts;

    @Transactional(rollbackFor = Exception.class)
    public TradeOrder prepare(String no) {
        var order = orders.findByOrderNoForUpdate(no);
        if (order == null
                || !"PENDING_PAYMENT".equals(order.status())
                || order.totalAmount().signum() <= 0) throw new IllegalStateException("订单不允许发起支付");
        var frozen = paymentFacts.paymentPreparedAmount(no);
        if (frozen != null) {
            if (frozen.compareTo(order.totalAmount()) != 0) {
                throw new IllegalStateException("订单应付与原支付准备金额不一致");
            }
            return order;
        }
        if (inbox.has("order.payment.prepare.v1", "PAYMENT_PREPARE", no)) {
            // 历史凭证只提供载荷哈希，按原序列化方式核验后才写订单业务列。
            var payload = new LinkedHashMap<String, Object>();
            payload.put("orderNo", no);
            payload.put("memberId", order.memberId());
            payload.put("amount", order.totalAmount());
            inbox.begin(
                    "order.payment.prepare.v1",
                    "PP:" + no,
                    "PAYMENT_PREPARE",
                    no,
                    JsonUtils.toJsonStr(payload));
        }
        if (!paymentFacts.freezePaymentAmount(no, order.totalAmount())) {
            throw new IllegalStateException("订单支付准备状态冲突");
        }
        return order;
    }
}
