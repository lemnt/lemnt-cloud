package com.lemnt.member.application.event.service;

import com.lemnt.common.client.event.CommerceEventEnvelope;
import com.lemnt.common.client.event.CommerceEventTopics;
import com.lemnt.member.application.growth.cmd.MemberTradeAssetCmd;
import com.lemnt.member.application.growth.service.MemberGrowthService;
import com.lemnt.member.domain.event.gateway.InboundEventGateway;
import com.lemnt.member.domain.event.gateway.MessageInboxGateway;
import com.lemnt.member.domain.growth.gateway.OrderBenefitsSnapshotGateway;
import com.lemnt.member.domain.growth.model.OrderBenefitsSnapshot;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * 消费凭证与积分、成长账本同事务，余额不足或前置事件未到必须回滚并重试。
 */
@Component
@RequiredArgsConstructor
public class OrderBenefitsEventHandler {
    private final MessageInboxGateway inbox;
    private final MemberGrowthService growth;
    private final OrderBenefitsSnapshotGateway orders;
    private final InboundEventGateway inboundEvents;

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void paid(CommerceEventEnvelope envelope) {
        envelope.requireType(CommerceEventTopics.ORDER_PAID);
        var v = inboundEvents.paidFacts(envelope.payload());
        if (v == null) throw new IllegalArgumentException("订单事实不能为空");
        check(envelope, v.orderNo(), v.orderNo());
        if (!begin(envelope, "member.order_paid.v1")) return;
        var o = original(v.orderNo(), v.memberId());
        if (!Set.of("PAID", "SHIPPED", "COMPLETED", "REFUNDED").contains(o.status())
            || v.amount() == null
            || v.amount().compareTo(o.amount()) != 0
            || v.usePoint() != o.usePoint()
            || v.givePoint() != o.givePoint()) throw new IllegalStateException("支付权益与原订单不一致");
        growth.tradeAsset(
            new MemberTradeAssetCmd(
                o.memberId(),
                "PAID",
                o.orderNo(),
                null,
                o.usePoint(),
                o.givePoint(),
                fen(o),
                0,
                false,
                0));
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void refunded(CommerceEventEnvelope envelope) {
        envelope.requireType(CommerceEventTopics.AFTER_SALE_COMPLETED);
        var v = inboundEvents.afterSaleFacts(envelope.payload());
        if (v == null) throw new IllegalArgumentException("售后事实不能为空");
        check(envelope, v.orderNo(), v.afterSaleNo());
        if (!begin(envelope, "member.after_sale_completed.v1")) return;
        if (!inbox.has("member.order_paid.v1", CommerceEventTopics.ORDER_PAID, v.orderNo()))
            throw new IllegalStateException("原订单支付权益尚未完成，请重试");
        var o = original(v.orderNo(), v.memberId());
        if (v.ruleVersion() != 1
            || v.quantity() < 1
            || v.amount() == null
            || v.amount().signum() < 0
            || v.returnUsePoint() < 0
            || v.deductGivePoint() < 0) throw new IllegalArgumentException("冻结售后权益不合法");
        growth.tradeAsset(
            new MemberTradeAssetCmd(
                o.memberId(),
                "REFUND",
                o.orderNo(),
                v.afterSaleNo(),
                o.usePoint(),
                o.givePoint(),
                fen(o),
                v.amount().movePointRight(2).longValueExact(),
                v.fullyRefunded(),
                v.returnUsePoint(),
                v.deductGivePoint(),
                v.ruleVersion()));
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void closed(CommerceEventEnvelope envelope) {
        envelope.requireType(CommerceEventTopics.ORDER_CLOSED);
        var v = inboundEvents.closedFacts(envelope.payload());
        if (v == null) throw new IllegalArgumentException("关单事实不能为空");
        check(envelope, v.orderNo(), v.orderNo());
        if (!begin(envelope, "member.order_closed.v1")) return;
        var o = original(v.orderNo(), v.memberId());
        if (!"CLOSED".equals(o.status())) throw new IllegalStateException("原订单未关闭");
        growth.tradeAsset(
            new MemberTradeAssetCmd(
                o.memberId(),
                "CLOSED",
                o.orderNo(),
                null,
                o.usePoint(),
                o.givePoint(),
                fen(o),
                0,
                false,
                0));
    }

    @Transactional(rollbackFor = Exception.class, isolation = Isolation.READ_COMMITTED)
    public void shipped(CommerceEventEnvelope envelope) {
        envelope.requireType(CommerceEventTopics.ORDER_SHIPPED);
        var v = inboundEvents.shippedFacts(envelope.payload());
        if (v == null) throw new IllegalArgumentException("发货事实不能为空");
        check(envelope, v.orderNo(), v.orderNo());
        if (!begin(envelope, "member.order_shipped.v1")) return;
        var o = original(v.orderNo(), v.memberId());
        if (!Set.of("SHIPPED", "COMPLETED", "REFUNDED").contains(o.status()))
            throw new IllegalStateException("原订单未发货");
    }

    private boolean begin(CommerceEventEnvelope e, String consumer) {
        return inbox.begin(consumer, e.eventNo(), e.eventType(), e.businessKey(), e.payload());
    }

    private void check(CommerceEventEnvelope e, String no, String key) {
        if (!e.aggregateNo().equals(no) || !e.businessKey().equals(key))
            throw new IllegalArgumentException("事件订单或业务编号不一致");
    }

    private long fen(OrderBenefitsSnapshot v) {
        return v.amount().movePointRight(2).longValueExact();
    }

    private OrderBenefitsSnapshot original(String no, Long member) {
        var v = orders.find(no);
        if (v == null
            || !v.orderNo().equals(no)
            || !v.memberId().equals(member)
            || v.amount() == null
            || v.usePoint() == null
            || v.givePoint() == null) throw new IllegalStateException("原订单权益快照缺失");
        return v;
    }
}
