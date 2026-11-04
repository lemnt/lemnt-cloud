package com.lemnt.order.application.order.service;

import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.order.domain.event.gateway.MessageInboxGateway;
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

    @Transactional(rollbackFor = Exception.class)
    public TradeOrder prepare(String no) {
        var order = orders.findByOrderNoForUpdate(no);
        if (order == null
            || !"PENDING_PAYMENT".equals(order.status())
            || order.totalAmount().signum() <= 0) throw new IllegalStateException("订单不允许发起支付");
        // 固定字段顺序，避免不同JVM的Map迭代顺序导致同一请求的载荷哈希不一致。
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
        return order;
    }
}
