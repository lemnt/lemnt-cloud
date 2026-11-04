package com.lemnt.order.application.order.service;

import com.lemnt.order.application.brokerage.service.BrokerageService;
import com.lemnt.order.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.order.domain.promotion.gateway.PromotionOrderGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/** 远端效果确认完成后才允许发货、售后及分佣，最后本地提交不包含远程写。 */
@Component
@RequiredArgsConstructor
public class OrderPaidCompletionService {
    private final OrderPersistenceService orders;
    private final OrderLifecycleService lifecycle;
    private final InventoryConfirmationGateway inventory;
    private final PromotionOrderGateway activities;
    private final BrokerageService brokerage;

    @Transactional(rollbackFor = Exception.class)
    public void finish(String no, Long activityRecord) {
        var o = orders.findByOrderNoForUpdate(no);
        if (o == null || !Set.of("PAID", "SHIPPED", "COMPLETED").contains(o.status()))
            throw new IllegalStateException("订单尚未支付");
        if (inventory.inventoryConfirmed(no)) return;
        var a = activities.lockByOrderNo(no);
        if (a != null && "COMBINATION".equals(a.activityType())) {
            if (activityRecord == null) throw new IllegalStateException("拼团参与结果尚未确认");
            if (a.activityRecordId() != null && !a.activityRecordId().equals(activityRecord))
                throw new IllegalStateException("拼团参与记录不一致");
            if (a.activityRecordId() == null && !activities.bindRecord(no, activityRecord))
                throw new IllegalStateException("拼团参与结果写入冲突");
        }
        lifecycle.inventoryConfirmed(no);
        brokerage.addOrderCommission(no);
    }
}
