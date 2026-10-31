package com.lemnt.mall.start.product;

import com.lemnt.mall.application.dubbo.SettlementLifecycleClientImpl;
import com.lemnt.mall.application.product.service.ProductInventoryService;
import com.lemnt.mall.application.promotion.service.PromotionOrderService;
import com.lemnt.mall.application.settlement.service.SettlementRecordService;
import com.lemnt.mall.client.bo.RefundStockBo;
import com.lemnt.mall.domain.member.gateway.MemberTradeGateway;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

/** 交易生命周期回调必须转发到活动订单补偿，普通订单无映射时由服务内部忽略。 */
class SettlementLifecyclePromotionTest {
    private final MemberTradeGateway members = Mockito.mock(MemberTradeGateway.class);
    private final SettlementRecordService records = Mockito.mock(SettlementRecordService.class);
    private final OrderGateway orders = Mockito.mock(OrderGateway.class);
    private final ProductInventoryService inventory = Mockito.mock(ProductInventoryService.class);
    private final PromotionOrderService promotions = Mockito.mock(PromotionOrderService.class);
    private final SettlementLifecycleClientImpl client =
            new SettlementLifecycleClientImpl(members, records, orders, inventory, promotions);

    private SettlementOrder order(String status) {
        return new SettlementOrder(7L, "TR1", 1L, new BigDecimal("10.00"), status,
                List.of(new SettlementItem(2L, "SKU", "商品", new BigDecimal("10.00"), 1,
                        new BigDecimal("10.00"), new BigDecimal("10.00"), BigDecimal.ZERO,
                        3L, "SPU", null, null)), null, 100L, 0L, null);
    }

    @Test
    void lifecycleCallbacksForwardToPromotionOrders() {
        Mockito.when(orders.findByOrderNo("TR1")).thenReturn(order("PENDING_PAYMENT"));
        client.reserved("TR1");
        Mockito.verify(promotions).reserved(Mockito.eq("TR1"), Mockito.any());

        Mockito.when(orders.findByOrderNo("TR1")).thenReturn(order("PAID"));
        client.paid("TR1");
        Mockito.verify(promotions).paid(Mockito.eq("TR1"), Mockito.any());

        Mockito.when(orders.findByOrderNo("TR1")).thenReturn(order("CLOSED"));
        client.closed("TR1");
        Mockito.verify(promotions).closed(Mockito.eq("TR1"));

        Mockito.when(orders.findByOrderNo("TR1")).thenReturn(order("REFUNDED"));
        var items = List.of(new RefundStockBo(2L, 1));
        client.refunded("TR1", "RF1", items, new BigDecimal("10.00"));
        Mockito.verify(promotions).refunded(Mockito.eq("TR1"), Mockito.eq("RF1"), Mockito.any(), Mockito.eq(items));
    }
}
