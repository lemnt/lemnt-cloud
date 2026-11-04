package com.lemnt.order.application.dubbo;

import com.lemnt.order.application.aftersale.service.AfterSaleRefundCoordinator;
import com.lemnt.order.application.order.service.OrderPaymentPreparationService;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.client.OrderSnapshotClient;
import com.lemnt.order.client.dto.BrokerageWithdrawalDTO;
import com.lemnt.order.client.dto.FrozenRefundDTO;
import com.lemnt.order.client.dto.OrderBenefitsSnapshotDTO;
import com.lemnt.order.client.dto.OrderPaymentSnapshotDTO;
import com.lemnt.order.client.dto.OrderPromotionResourcesDTO;
import com.lemnt.order.domain.aftersale.model.AfterSale;
import com.lemnt.order.domain.brokerage.gateway.BrokerageWithdrawGateway;
import com.lemnt.order.domain.order.gateway.OrderResourceGateway;
import com.lemnt.order.domain.order.model.TradeOperator;
import com.lemnt.order.domain.order.model.TradeOrder;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

/**
 * 原订单/售后快照的唯一提供方，RPC不暴露数据库模型。
 */
@DubboService
@RequiredArgsConstructor
public class OrderSnapshotClientImpl implements OrderSnapshotClient {
    private final OrderPersistenceService orders;
    private final OrderPaymentPreparationService payments;
    private final AfterSaleRefundCoordinator refunds;
    private final OrderResourceGateway resources;
    private final BrokerageWithdrawGateway withdraws;

    public BrokerageWithdrawalDTO brokerageWithdrawal(Long id) {
        var v = withdraws.find(id);
        return v == null
            ? null
            : new BrokerageWithdrawalDTO(
            v.id(), v.userId(), v.price().subtract(v.feePrice()), v.type(), v.status());
    }

    public OrderBenefitsSnapshotDTO benefitsSnapshot(String no) {
        var v = orders.findByOrderNo(no);
        return v == null
            ? null
            : new OrderBenefitsSnapshotDTO(
            no, v.memberId(), v.usePoint(), v.givePoint(), v.totalAmount(), v.status());
    }

    public OrderPaymentSnapshotDTO preparePayment(String no) {
        return payment(payments.prepare(no));
    }

    public OrderPaymentSnapshotDTO paymentOrder(String no) {
        return payment(orders.findByOrderNo(no));
    }

    private OrderPaymentSnapshotDTO payment(TradeOrder v) {
        return v == null
            ? null
            : new OrderPaymentSnapshotDTO(
            v.orderNo(), v.memberId(), v.totalAmount(), v.status());
    }

    public OrderPromotionResourcesDTO promotionResources(String no) {
        var v = resources.plan(no);
        return new OrderPromotionResourcesDTO(
            v.orderId(),
            v.orderNo(),
            v.memberId(),
            v.couponId(),
            v.activityType(),
            v.activityId(),
            v.activityRecordId(),
            v.headId(),
            v.lines().stream()
                .map(
                    l ->
                        new OrderPromotionResourcesDTO.Line(
                            l.spuId(), l.skuId(), l.quantity()))
                .toList(),
            v.status());
    }

    public FrozenRefundDTO prepareRefund(String no, Long operator) {
        return refund(
            refunds.prepare(
                no,
                operator == null
                    ? TradeOperator.system()
                    : TradeOperator.administrator(operator)));
    }

    public FrozenRefundDTO refundPlan(String no) {
        return refund(refunds.plan(no));
    }

    private FrozenRefundDTO refund(AfterSale v) {
        return new FrozenRefundDTO(
            v.refundNo(),
            v.orderNo(),
            v.memberId(),
            v.paymentNo(),
            v.amount(),
            v.refundRuleVersion(),
            v.status());
    }
}
