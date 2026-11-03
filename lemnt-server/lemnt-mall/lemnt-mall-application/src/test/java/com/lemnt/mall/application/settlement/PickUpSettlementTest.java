package com.lemnt.mall.application.settlement;

import com.lemnt.mall.application.settlement.service.SettlementDeliveryService;
import com.lemnt.mall.domain.settlement.model.SettlementDelivery;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementQuote;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/** 自提报价不计算运费，冻结门店编号。 */
class PickUpSettlementTest {
    final SettlementDeliveryService service =
            new SettlementDeliveryService(
                    mock(com.lemnt.mall.domain.member.gateway.MemberDeliveryAddressGateway.class),
                    mock(com.lemnt.mall.domain.settlement.gateway.DeliveryPriceGateway.class),
                    mock(com.lemnt.mall.domain.member.gateway.MemberTradeGateway.class));
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
        assertThrows(
                com.lemnt.common.core.exception.ServiceException.class,
                () -> service.pickUp(quote, null));
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
        assertNotNull(
                com.lemnt.mall.domain.settlement.model.SettlementRules.quoteToken(
                        service.pickUp(quote, 9L)));
    }
}
