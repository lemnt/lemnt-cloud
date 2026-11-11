package com.lemnt.order.application.settlement;

import com.lemnt.order.application.settlement.service.LocalDeliveryPriceService;
import com.lemnt.order.application.settlement.service.SettlementDeliveryService;
import com.lemnt.order.application.settlement.service.SettlementPromotionSnapshots;
import com.lemnt.order.domain.member.gateway.MemberDeliveryAddressGateway;
import com.lemnt.order.domain.member.gateway.MemberTradeGateway;
import com.lemnt.order.domain.member.model.MemberDeliveryAddress;
import com.lemnt.order.domain.member.model.MemberPointConfig;
import com.lemnt.order.domain.product.model.ProductSnapshot;
import com.lemnt.order.domain.settlement.model.DeliveryPriceResult;
import com.lemnt.order.domain.settlement.model.SettlementItem;
import com.lemnt.order.domain.settlement.model.SettlementQuote;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 满减包邮保存应收与减免，现金不增加，赠积分及赠券保持。
 */
class SettlementDeliveryFreeShippingTest {
    final MemberDeliveryAddressGateway addresses = mock(MemberDeliveryAddressGateway.class);
    final LocalDeliveryPriceService prices = mock(LocalDeliveryPriceService.class);
    final MemberTradeGateway memberTrade = mock(MemberTradeGateway.class);
    final SettlementDeliveryService service =
        new SettlementDeliveryService(addresses, prices, memberTrade);

    @Test
    void freeDeliverySnapshotsOriginalFeeAndPreservesRewards() {
        when(addresses.get(1L, 10L))
            .thenReturn(
                new MemberDeliveryAddress(10L, 1L, "张三", "13800000000", 110101L, "验收路1号"));
        when(memberTrade.config()).thenReturn(new MemberPointConfig(true, 1, 0, 2));
        var product =
            new ProductSnapshot(
                5L,
                "SKU",
                "商品",
                BigDecimal.TEN,
                10,
                List.of(1),
                2L,
                BigDecimal.ONE,
                BigDecimal.ONE,
                3L,
                "SPU",
                null,
                null);
        var item =
            new SettlementItem(
                5L,
                "SKU",
                "商品",
                BigDecimal.TEN,
                1,
                BigDecimal.TEN,
                BigDecimal.TEN,
                BigDecimal.ZERO,
                3L,
                "SPU",
                null,
                null);
        when(prices.price(eq(110101), anyList()))
            .thenReturn(
                new DeliveryPriceResult(
                    new BigDecimal("5.00"), Map.of(5L, new BigDecimal("5.00"))));
        var reward =
            SettlementPromotionSnapshots.capture(
                "REWARD:1",
                "REWARD",
                1L,
                "满减包邮",
                "{\"limit\":10}",
                List.of(item),
                Map.of(5L, BigDecimal.TEN),
                Map.of(5L, BigDecimal.TEN),
                0L,
                5L,
                Map.of(7L, 2));
        var quote =
            new SettlementQuote(
                List.of(item),
                BigDecimal.TEN,
                BigDecimal.ZERO,
                BigDecimal.TEN,
                null,
                0L,
                20L,
                null,
                5L,
                true,
                Map.of(7L, 2),
                List.of(reward),
                BigDecimal.ZERO.setScale(2),
                BigDecimal.ZERO.setScale(2));

        var result = service.attach(1L, 10L, Map.of(5L, product), quote);

        assertEquals(BigDecimal.ZERO.setScale(2), result.delivery().deliveryPrice());
        assertEquals(BigDecimal.ZERO.setScale(2), result.items().get(0).deliveryPrice());
        assertEquals(0, result.payableAmount().compareTo(BigDecimal.TEN));
        assertEquals(25L, result.givePoint());
        assertEquals(Map.of(7L, 2), result.giveCouponTemplateCounts());
        assertTrue(result.freeDelivery());
        assertEquals(new BigDecimal("5.00"), result.deliveryOriginalPrice());
        assertEquals(new BigDecimal("5.00"), result.deliveryDiscountPrice());
        assertEquals(new BigDecimal("5.00"), result.items().get(0).deliveryDiscountPrice());
        assertEquals(25L, result.items().get(0).givePoint());
        verify(prices).price(eq(110101), anyList());
    }
}
