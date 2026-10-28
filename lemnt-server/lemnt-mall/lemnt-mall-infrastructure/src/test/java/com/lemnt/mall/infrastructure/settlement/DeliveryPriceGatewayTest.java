package com.lemnt.mall.infrastructure.settlement;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.domain.settlement.model.DeliveryPriceLine;
import com.lemnt.mall.infrastructure.settlement.gateway.DeliveryPriceGatewayImpl;
import com.lemnt.trade.client.TradeDeliveryClient;
import com.lemnt.trade.client.dto.DeliveryFeeDTO;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 远程运费边界保证元/分精确、行金额守恒，不能将远程异常结果当免费。 */
class DeliveryPriceGatewayTest {
    @Test
    void exactMoneyConversionAndRemoteAllocationsAreValidated() {
        var client = mock(TradeDeliveryClient.class);
        var gateway = new DeliveryPriceGatewayImpl();
        ReflectionTestUtils.setField(gateway, "client", client);
        var input = List.of(new DeliveryPriceLine(5L, 1L, 2, null, null, new BigDecimal("20.00")));
        when(client.price(any())).thenAnswer(call -> {
            com.lemnt.trade.client.bo.DeliveryFeeBo request = call.getArgument(0);
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
        var gateway = new DeliveryPriceGatewayImpl();
        ReflectionTestUtils.setField(gateway, "client", client);
        assertThrows(ServiceException.class, () -> gateway.price(110101,
                List.of(new DeliveryPriceLine(5L, 1L, 1, null, null, new BigDecimal("21474836.48")))));
        verifyNoInteractions(client);
    }
}
