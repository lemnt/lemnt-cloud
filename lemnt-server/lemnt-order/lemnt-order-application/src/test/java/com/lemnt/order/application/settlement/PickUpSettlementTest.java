package com.lemnt.order.application.settlement;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.order.application.settlement.service.LocalDeliveryPriceService;
import com.lemnt.order.application.settlement.service.SettlementDeliveryService;
import com.lemnt.order.domain.member.gateway.MemberDeliveryAddressGateway;
import com.lemnt.order.domain.member.gateway.MemberTradeGateway;
import com.lemnt.order.domain.settlement.model.SettlementItem;
import com.lemnt.order.domain.settlement.model.SettlementItemPromotion;
import com.lemnt.order.domain.settlement.model.SettlementPromotion;
import com.lemnt.order.domain.settlement.model.SettlementQuote;
import com.lemnt.order.domain.settlement.model.SettlementRules;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * 自提报价不计算运费，冻结门店编号。
 */
class PickUpSettlementTest {
    final SettlementDeliveryService service =
            new SettlementDeliveryService(
                    mock(MemberDeliveryAddressGateway.class),
                    mock(LocalDeliveryPriceService.class),
                    mock(MemberTradeGateway.class));
    final SettlementItem item =
            new SettlementItem(
                    5L,
                    "P1",
                    "商品",
                    new BigDecimal("10.00"),
                    2,
                    new BigDecimal("20.00"),
                    new BigDecimal("18.00"),
                    BigDecimal.ZERO,
                    1L,
                    "商品",
                    List.of(),
                    null);

    @Test
    void pickupKeepsPayableAndSetsZeroDelivery() {
        var quote =
                new SettlementQuote(
                        List.of(item),
                        new BigDecimal("20.00"),
                        new BigDecimal("2.00"),
                        new BigDecimal("18.00"),
                        null,
                        0L,
                        0L,
                        null);
        var result = service.pickUp(quote, 9L);
        assertNull(result.delivery().addressId());
        assertEquals(2, result.delivery().deliveryType());
        assertEquals(9L, result.delivery().pickUpStoreId());
        assertEquals(new BigDecimal("0.00"), result.delivery().deliveryPrice());
        assertEquals(new BigDecimal("18.00"), result.payableAmount());
        assertEquals(new BigDecimal("0.00"), result.items().get(0).deliveryPrice());
    }

    @Test
    void missingStoreIsRejected() {
        var quote =
                new SettlementQuote(
                        List.of(item),
                        new BigDecimal("20.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("20.00"),
                        null);
        assertThrows(ServiceException.class, () -> service.pickUp(quote, null));
    }

    @Test
    void pickupQuoteTokenSupportsMissingReceiver() {
        var quote =
                new SettlementQuote(
                        List.of(item),
                        new BigDecimal("20.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("20.00"),
                        null);
        assertNotNull(SettlementRules.quoteToken(service.pickUp(quote, 9L)));
    }

    @Test
    void pickupKeepsFixedProductGiftAndDoesNotAddItTwice() {
        var zero = new BigDecimal("0.00");
        var sources =
                List.of(
                        new SettlementPromotion(
                                null,
                                null,
                                "PRODUCT_POINT:5",
                                "PRODUCT_POINT",
                                1L,
                                "商品赠积分",
                                null,
                                "{\"giveIntegral\":7}",
                                zero,
                                zero,
                                0L,
                                7L,
                                Map.of(),
                                List.of(
                                        new SettlementItemPromotion(
                                                null, null, null, "0", zero, zero, 0L, 7L))),
                        new SettlementPromotion(
                                null,
                                null,
                                "ORDER_GIFT",
                                "ORDER_GIFT",
                                null,
                                "支付赠积分",
                                null,
                                "{\"givePerYuan\":2}",
                                zero,
                                zero,
                                0L,
                                36L,
                                Map.of(),
                                List.of(
                                        new SettlementItemPromotion(
                                                null, null, null, "0", zero, zero, 0L, 36L))));
        var quote =
                new SettlementQuote(
                        List.of(item),
                        new BigDecimal("20.00"),
                        new BigDecimal("2.00"),
                        new BigDecimal("18.00"),
                        null,
                        0L,
                        43L,
                        null,
                        0L,
                        false,
                        Map.of(),
                        sources,
                        zero,
                        zero);

        var pickedUp = service.pickUp(quote, 9L);
        assertEquals(43L, pickedUp.givePoint());
        assertEquals(43L, pickedUp.items().get(0).givePoint());
        assertEquals(7L, pickedUp.promotions().get(0).givePoint());
        assertEquals(36L, pickedUp.promotions().get(1).givePoint());
    }
}
