package com.lemnt.order.application.dubbo;

import com.lemnt.order.client.CheckoutLifecycleClient;
import com.lemnt.order.client.bo.RefundBenefitsBo;
import com.lemnt.order.client.bo.RefundStockBo;
import com.lemnt.order.domain.order.gateway.OrderBenefitsGateway;
import com.lemnt.order.domain.order.model.OrderRefundBenefits;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/** 兼容原内部入口；权益投递统一复用订单保存的业务事件。 */
@DubboService
@RequiredArgsConstructor
public class SettlementLifecycleClientImpl implements CheckoutLifecycleClient {
    private final OrderBenefitsGateway benefits;

    public void verify(String no) {
        benefits.verify(no);
    }

    public void reserved(String no) {
        benefits.reserved(no);
    }

    public void paid(String no) {
        benefits.paid(no);
    }

    public void closed(String no) {
        benefits.closed(no);
    }

    public void refunded(RefundBenefitsBo v) {
        benefits.refunded(
                new OrderRefundBenefits(
                        v.orderNo(),
                        v.afterSaleNo(),
                        v.orderItemId(),
                        v.skuId(),
                        v.quantity(),
                        v.amount(),
                        v.returnUsePoint(),
                        v.deductGivePoint(),
                        v.ruleVersion()));
    }

    public void refunded(String no, String refundNo, List<RefundStockBo> lines, BigDecimal amount) {
        benefits.refunded(
                no,
                refundNo,
                lines.stream()
                        .collect(
                                Collectors.toMap(
                                        RefundStockBo::productId, RefundStockBo::quantity)),
                amount);
    }
}
