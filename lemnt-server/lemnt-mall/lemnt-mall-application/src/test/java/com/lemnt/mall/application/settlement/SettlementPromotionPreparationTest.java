package com.lemnt.mall.application.settlement;

import com.lemnt.mall.application.member.service.MemberValidationService;
import com.lemnt.mall.application.promotion.service.ActivityPricingService;
import com.lemnt.mall.application.settlement.service.SettlementDeliveryService;
import com.lemnt.mall.application.settlement.service.SettlementPreparationService;
import com.lemnt.mall.application.settlement.service.SettlementRecordService;
import com.lemnt.mall.domain.cart.gateway.CartGateway;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.member.gateway.MemberLockGateway;
import com.lemnt.mall.domain.member.model.MemberSnapshot;
import com.lemnt.mall.domain.product.gateway.ProductCatalogGateway;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.gateway.SettlementGateway;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;
import com.lemnt.mall.domain.settlement.model.SettlementQuote;
import com.lemnt.mall.domain.settlement.model.SettlementRecord;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 活动结算准备复用请求编号，活动价格快照不经过普通促销计价。 */
class SettlementPromotionPreparationTest {
    private final ActivityPricingService pricing = Mockito.mock(ActivityPricingService.class);
    private final SettlementDeliveryService delivery = Mockito.mock(SettlementDeliveryService.class);
    private final CartGateway cart = Mockito.mock(CartGateway.class);
    private final MemberLockGateway locks = Mockito.mock(MemberLockGateway.class);
    private final SettlementRecordService records = Mockito.mock(SettlementRecordService.class);
    private final CouponGateway coupons = Mockito.mock(CouponGateway.class);
    private final CouponTemplateGateway templates = Mockito.mock(CouponTemplateGateway.class);
    private final OrderGateway orders = Mockito.mock(OrderGateway.class);
    private final SettlementGateway cache = Mockito.mock(SettlementGateway.class);
    private final ProductCatalogGateway catalog = Mockito.mock(ProductCatalogGateway.class);
    private final MemberValidationService members = Mockito.mock(MemberValidationService.class);
    private final SettlementPreparationService service = new SettlementPreparationService(pricing, delivery,
            cart, locks, records, coupons, templates, orders, cache, catalog, members);

    private SettlementQuote quote() {
        var item = new SettlementItem(2L, "SKU", "商品", new BigDecimal("10.00"), 1,
                new BigDecimal("10.00"), new BigDecimal("5.00"), BigDecimal.ZERO, 3L, "SPU", null, null);
        return new SettlementQuote(List.of(item), new BigDecimal("10.00"), new BigDecimal("5.00"),
                new BigDecimal("5.00"), null, 100L, 0L);
    }

    @Test
    void newRequestPersistsQuoteAndReturnsCreatedOrder() {
        Mockito.when(records.findRequest(1L, "REQ1")).thenReturn(null);
        Mockito.when(members.requireActive(1L)).thenReturn(new MemberSnapshot(1L, 1, 100, 0L));
        var result = service.preparePromotion(1L, "REQ1", "FP", this::quote, List.of());
        assertTrue(result.created());
        assertEquals(new BigDecimal("5.00"), result.order().totalAmount());
        assertEquals(100L, result.order().usePoint());
        Mockito.verify(records).save(Mockito.any(SettlementRecord.class), Mockito.any(SettlementOrder.class));
    }

    @Test
    void repeatedRequestReturnsOriginalSnapshotWithoutPricing() {
        var previous = new SettlementOrder(7L, "TR1", 1L, new BigDecimal("5.00"), "PENDING_PAYMENT",
                quote().items(), null, 100L, 0L, null);
        Mockito.when(records.findRequest(1L, "REQ1")).thenReturn(new SettlementRecord("TR1", 1L, "REQ1",
                "FP", new BigDecimal("10.00"), new BigDecimal("5.00"), new BigDecimal("5.00"), null,
                List.of()));
        Mockito.when(orders.findByOrderNo("TR1")).thenReturn(previous);
        var result = service.preparePromotion(1L, "REQ1", "FP", () -> {
            throw new IllegalStateException("不应重新计价");
        }, List.of());
        assertFalse(result.created());
        assertSame(previous, result.order());
        Mockito.verifyNoInteractions(pricing);
    }

    @Test
    void changedFingerprintIsRejected() {
        Mockito.when(records.findRequest(1L, "REQ1")).thenReturn(new SettlementRecord("TR1", 1L, "REQ1",
                "OTHER", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, null, List.of()));
        assertThrows(IllegalStateException.class,
                () -> service.preparePromotion(1L, "REQ1", "FP", this::quote, List.of()));
    }
}
