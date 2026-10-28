package com.lemnt.trade.application.paymentrefund.service;

import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefund;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import java.util.LinkedHashMap;

/** 渠道退款已提交后再远程处理库存与券，失败可重放同一售后单。 */
@Component
@RequiredArgsConstructor
public class RefundBenefitsService {
    private final AfterSaleGateway afterSales;
    private final OrderPersistenceService orders;
    private final TradeOrderItemGateway items;
    private final OrderBenefitsGateway benefits;

    public void complete(PaymentRefund refund) {
        if (!"SUCCESS".equals(refund.status())) { return; }
        var afterSale = afterSales.afterSale(refund.afterSaleNo());
        var order = orders.findByOrderNo(refund.orderNo());
        if (afterSale == null || order == null || !"REFUNDED".equals(afterSale.status())) {
            throw new IllegalStateException("退款成功但关联售后状态不一致");
        }
        var quantities = new LinkedHashMap<Long, Integer>();
        var lines = items.refundItems(order.id());
        if (afterSale.orderItemId() == null) {
            lines.forEach(item -> quantities.put(item.productId(), item.quantity()));
        } else {
            var item = lines.stream().filter(line -> line.id().equals(afterSale.orderItemId())).findFirst()
                    .orElseThrow(() -> new IllegalStateException("售后商品不属于原订单"));
            quantities.put(item.productId(), afterSale.quantity());
        }
        benefits.refunded(order.orderNo(), afterSale.refundNo(), quantities, refund.amount());
    }
}
