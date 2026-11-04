package com.lemnt.order.application.settlement;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.order.application.settlement.service.LocalDeliveryPriceService;
import com.lemnt.order.client.TradeDeliveryClient;
import com.lemnt.order.client.bo.DeliveryFeeBo;
import com.lemnt.order.client.dto.DeliveryFeeDTO;
import com.lemnt.order.domain.settlement.model.DeliveryPriceLine;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 远程运费边界保证元/分精确、行金额守恒，不能将远程异常结果当免费。
 */
class LocalDeliveryPriceServiceTest {
    @Test
    void exactMoneyConversionAndRemoteAllocationsAreValidated() {
        var client = mock(TradeDeliveryClient.class);
        var gateway = new LocalDeliveryPriceService(client);
        var input = List.of(new DeliveryPriceLine(5L, 1L, 2, null, null, new BigDecimal("20.00")));
        when(client.price(any()))
            .thenAnswer(
                call -> {
                    DeliveryFeeBo request = call.getArgument(0);
                    assertEquals(2000, request.items().get(0).payPrice());
                    return new DeliveryFeeDTO(650, Map.of(5L, 650));
                });
        assertEquals(new BigDecimal("6.50"), gateway.price(110101, input).total());
        doReturn(new DeliveryFeeDTO(650, Map.of(5L, 649))).when(client).price(any());
        assertThrows(IllegalStateException.class, () -> gateway.price(110101, input));
        doReturn(new DeliveryFeeDTO(650, Map.of(6L, 650))).when(client).price(any());
        assertThrows(IllegalStateException.class, () -> gateway.price(110101, input));
    }

    @Test
    void unsupportedAmountIsRejectedBeforeRemoteCall() {
        var client = mock(TradeDeliveryClient.class);
        var gateway = new LocalDeliveryPriceService(client);
        assertThrows(
            ServiceException.class,
            () ->
                gateway.price(
                    110101,
                    List.of(
                        new DeliveryPriceLine(
                            5L,
                            1L,
                            1,
                            null,
                            null,
                            new BigDecimal("21474836.48")))));
        verifyNoInteractions(client);
    }
}
