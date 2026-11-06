package com.lemnt.order.application.dubbo;

import com.lemnt.order.client.bo.RefundBenefitsBo;
import com.lemnt.order.client.bo.RefundStockBo;
import com.lemnt.order.domain.order.gateway.OrderBenefitsGateway;
import com.lemnt.order.domain.order.model.OrderRefundBenefits;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * 原生命周期兼容入口仍完整转发，实际权益效果已由订单冻结事件负责。
 */
class SettlementLifecyclePromotionTest {
    @Test
    void lifecycleCallbacksForwardToOwnedOrderBenefits() {
        var benefits = mock(OrderBenefitsGateway.class);
        var client = new SettlementLifecycleClientImpl(benefits);
        client.verify("TR1");
        client.reserved("TR1");
        client.paid("TR1");
        client.closed("TR1");
        verify(benefits).verify("TR1");
        verify(benefits).reserved("TR1");
        verify(benefits).paid("TR1");
        verify(benefits).closed("TR1");
        client.refunded("TR1", "RF1", List.of(new RefundStockBo(2L, 1)), new BigDecimal("10.00"));
        verify(benefits).refunded("TR1", "RF1", Map.of(2L, 1), new BigDecimal("10.00"));
        client.refunded(
            new RefundBenefitsBo("TR1", "RF2", 11L, 2L, 1, new BigDecimal("5.00"), 50, 2, 1));
        verify(benefits)
            .refunded(
                new OrderRefundBenefits(
                    "TR1", "RF2", 11L, 2L, 1, new BigDecimal("5.00"), 50, 2, 1));
    }
}
