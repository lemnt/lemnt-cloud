package com.lemnt.trade.application.paymentrefund.service;

import com.lemnt.trade.application.brokerage.service.BrokerageService;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.aftersale.model.AfterSale;
import com.lemnt.trade.domain.order.gateway.OrderBenefitsGateway;
import com.lemnt.trade.domain.order.gateway.TradeOrderItemGateway;
import com.lemnt.trade.domain.order.model.OrderRefundBenefits;
import com.lemnt.trade.domain.order.model.TradeOrder;
import com.lemnt.trade.domain.paymentrefund.gateway.PaymentRefundGateway;
import com.lemnt.trade.domain.paymentrefund.model.PaymentRefund;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

/** 渠道退款已提交后再远程处理库存与券，失败可重放同一售后单。 */
@Component
@RequiredArgsConstructor
public class RefundBenefitsService {
    private final AfterSaleGateway afterSales;
    private final OrderPersistenceService orders;
    private final TradeOrderItemGateway items;
    private final OrderBenefitsGateway benefits;
    private final BrokerageService brokerage;
    private final PaymentRefundGateway paymentRefunds;

    public void complete(PaymentRefund refund) {
        if (!"SUCCESS".equals(refund.status())) {
            return;
        }
        var afterSale = afterSales.afterSale(refund.afterSaleNo());
        var order = orders.findByOrderNo(refund.orderNo());
        if (afterSale == null || order == null || !"REFUNDED".equals(afterSale.status())) {
            throw new IllegalStateException("退款成功但关联售后状态不一致");
        }
        apply(afterSale, order);
        recall(refund.orderNo(), refund.amount());
    }

    /** 新版含零现金售后，只有已完成退单可执行冻结权益。 */
    public void completeAfterSale(String afterSaleNo) {
        var afterSale = afterSales.afterSale(afterSaleNo);
        if (afterSale == null || !"REFUNDED".equals(afterSale.status())) {
            throw new IllegalStateException("售后尚未完成");
        }
        var order = orders.findByOrderNo(afterSale.orderNo());
        if (order == null) {
            throw new IllegalStateException("原订单不存在");
        }
        apply(afterSale, order);
        recall(afterSale.orderNo(), afterSale.amount());
    }

    /** 新版明确行与积分，历史未知保留原协议。 */
    private void apply(
            AfterSale afterSale,
            TradeOrder order) {
        var quantities = new LinkedHashMap<Long, Integer>();
        var lines = items.refundItems(order.id());
        if (afterSale.orderItemId() == null) {
            lines.forEach(item -> quantities.put(item.productId(), item.quantity()));
        } else {
            var item =
                    lines.stream()
                            .filter(line -> line.id().equals(afterSale.orderItemId()))
                            .findFirst()
                            .orElseThrow(() -> new IllegalStateException("售后商品不属于原订单"));
            quantities.put(item.productId(), afterSale.quantity());
        }
        if (afterSale.returnUsePoint() != null
                && afterSale.deductGivePoint() != null
                && Integer.valueOf(1).equals(afterSale.refundRuleVersion())) {
            var item =
                    lines.stream()
                            .filter(l -> l.id().equals(afterSale.orderItemId()))
                            .findFirst()
                            .orElseThrow();
            benefits.refunded(
                    new OrderRefundBenefits(
                            order.orderNo(),
                            afterSale.refundNo(),
                            afterSale.orderItemId(),
                            item.productId(),
                            afterSale.quantity(),
                            afterSale.amount(),
                            afterSale.returnUsePoint(),
                            afterSale.deductGivePoint(),
                            afterSale.refundRuleVersion()));
        } else {
            benefits.refunded(
                    order.orderNo(), afterSale.refundNo(), quantities, afterSale.amount());
        }
    }

    /** 佣金沿用现金累计额回收，零现金行不会额外回收现金佣金。 */
    private void recall(String orderNo, BigDecimal amount) {
        var latest = orders.findByOrderNo(orderNo);
        if (latest != null && "REFUNDED".equals(latest.status())) {
            brokerage.cancelOrderCommission(orderNo);
        } else if (latest != null) {
            // 部分退款按累计成功退款金额同比例扣回佣金，重复回调只补差额。
            var total = paymentRefunds.total(orderNo);
            brokerage.recallOrderCommission(
                    orderNo, total == null ? amount : total.refundAmount(), latest.totalAmount());
        }
    }
}
