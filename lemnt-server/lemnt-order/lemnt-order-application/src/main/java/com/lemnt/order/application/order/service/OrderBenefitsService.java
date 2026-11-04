package com.lemnt.order.application.order.service;

import com.lemnt.order.application.settlement.service.SettlementRecordService;
import com.lemnt.order.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.order.domain.event.gateway.OrderEventGateway;
import com.lemnt.order.domain.member.gateway.MemberTradeGateway;
import com.lemnt.order.domain.member.model.MemberTradeChange;
import com.lemnt.order.domain.order.gateway.OrderBenefitsGateway;
import com.lemnt.order.domain.order.gateway.OrderResourceGateway;
import com.lemnt.order.domain.order.gateway.PromotionResourceGateway;
import com.lemnt.order.domain.order.model.OrderRefundBenefits;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** 下单同步冻结；支付、关单和售后权益只由已提交的订单事件驱动。 */
@Component
@RequiredArgsConstructor
public class OrderBenefitsService implements OrderBenefitsGateway {
    private final OrderPersistenceService orders;
    private final OrderResourceGateway plans;
    private final PromotionResourceGateway promotion;
    private final MemberTradeGateway members;
    private final SettlementRecordService records;
    private final AfterSaleGateway afterSales;
    private final OrderEventGateway events;

    public void verify(String no) {
        var order = orders.findByOrderNo(no);
        if (order == null
                || !Set.of("RESERVING", "PENDING_PAYMENT", "PAID", "SHIPPED", "COMPLETED")
                        .contains(order.status())) throw new IllegalStateException("订单不能继续下单");
        if (Set.of("RESERVING", "PENDING_PAYMENT").contains(order.status())) {
            promotion.lock(plans.plan(no));
            members.apply(
                    new MemberTradeChange(
                            order.memberId(),
                            "CREATE",
                            no,
                            null,
                            order.usePoint(),
                            order.givePoint(),
                            order.totalAmount().movePointRight(2).longValueExact(),
                            0,
                            false,
                            0));
        }
    }

    public void reserved(String no) {
        var o = orders.findByOrderNo(no);
        if (o == null
                || !Set.of("PENDING_PAYMENT", "PAID", "SHIPPED", "COMPLETED").contains(o.status()))
            throw new IllegalStateException("订单尚未完成库存预占");
        if ("PENDING_PAYMENT".equals(o.status())) records.consumeCart(no, o.memberId());
    }

    public void paid(String no) {
        var o = orders.findByOrderNo(no);
        if (o == null || !Set.of("PAID", "SHIPPED", "COMPLETED", "REFUNDED").contains(o.status()))
            throw new IllegalStateException("订单未支付");
    }

    @Transactional(rollbackFor = Exception.class)
    public void closed(String no) {
        var o = orders.findByOrderNoForUpdate(no);
        if (o == null || !"CLOSED".equals(o.status())) throw new IllegalStateException("订单未关闭");
        events.closed(o);
    }

    @Transactional(rollbackFor = Exception.class)
    public void refunded(OrderRefundBenefits v) {
        var af = afterSales.afterSale(v.afterSaleNo());
        if (af == null
                || !"REFUNDED".equals(af.status())
                || !Objects.equals(v.orderNo(), af.orderNo())
                || !Objects.equals(v.orderItemId(), af.orderItemId())
                || !Objects.equals(v.skuId(), af.skuId())
                || !Objects.equals(v.quantity(), af.quantity())
                || v.amount().compareTo(af.amount()) != 0
                || !Objects.equals(v.returnUsePoint(), af.returnUsePoint())
                || !Objects.equals(v.deductGivePoint(), af.deductGivePoint())
                || !Objects.equals(v.ruleVersion(), af.refundRuleVersion()))
            throw new IllegalStateException("售后冻结事实不一致");
        if (!events.replayAfterSale(af.refundNo()))
            throw new IllegalStateException("原售后事件不存在或仍在发送，请核对迁移");
    }

    public void refunded(String no, String af, Map<Long, Integer> quantities, BigDecimal amount) {
        throw new IllegalStateException("历史售后缺少冻结方案，须先审核迁移");
    }
}
