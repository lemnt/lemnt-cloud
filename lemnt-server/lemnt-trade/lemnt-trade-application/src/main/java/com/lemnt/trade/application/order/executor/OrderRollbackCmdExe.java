package com.lemnt.trade.application.order.executor;

import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 异步消费订单补偿消息，由商城统一回滚库存与优惠券。 */
@Component
@RequiredArgsConstructor
public class OrderRollbackCmdExe {
    private final OrderPersistenceService orders;
    private final OrderBenefitsGateway benefits;

    /** 已关闭订单通过商城的单一远程接口补偿，异常交由 MQ 重试。 */
    public void execute(String orderNo) {
        var order = orders.findByOrderNo(orderNo);
        if (order == null || !"CLOSED".equals(order.status())) {
            throw new IllegalStateException("订单未关闭，不能回滚库存");
        }
        benefits.closed(orderNo);
    }
}
