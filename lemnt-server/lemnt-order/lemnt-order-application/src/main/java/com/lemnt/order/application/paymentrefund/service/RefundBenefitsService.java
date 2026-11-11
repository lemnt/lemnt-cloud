package com.lemnt.order.application.paymentrefund.service;

import com.lemnt.order.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.order.domain.event.gateway.OrderEventGateway;
import com.lemnt.order.domain.paymentrefund.model.PaymentRefund;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 管理重试只重投原事件，不能按当前整单状态改写旧售后的冻结权益或全退标记。
 */
@Component
@RequiredArgsConstructor
public class RefundBenefitsService {
    private final AfterSaleGateway afterSales;
    private final OrderEventGateway events;

    @Transactional(rollbackFor = Exception.class)
    public void complete(PaymentRefund v) {
        if (!"SUCCESS".equals(v.status())) return;
        completeAfterSale(v.afterSaleNo());
    }

    @Transactional(rollbackFor = Exception.class)
    public void completeAfterSale(String no) {
        var af = afterSales.afterSale(no);
        if (af == null || !"REFUNDED".equals(af.status()))
            throw new IllegalStateException("售后尚未完成");
        if (!events.replayAfterSale(no)) throw new IllegalStateException("原售后事件不存在或仍在投递，请核对迁移");
    }
}
