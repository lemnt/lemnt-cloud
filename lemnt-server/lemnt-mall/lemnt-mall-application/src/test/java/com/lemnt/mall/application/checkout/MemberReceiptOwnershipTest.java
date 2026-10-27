package com.lemnt.mall.application.checkout;

import com.lemnt.mall.application.checkout.executor.CheckoutReceiptQueryExe;
import com.lemnt.mall.application.checkout.service.CheckoutService;
import com.lemnt.mall.domain.order.gateway.OrderGateway;
import com.lemnt.mall.domain.checkout.gateway.CartGateway;
import com.lemnt.mall.domain.checkout.gateway.CheckoutAccountGateway;
import com.lemnt.mall.domain.checkout.gateway.CheckoutCartGateway;
import com.lemnt.mall.domain.checkout.gateway.CouponGateway;
import com.lemnt.mall.domain.checkout.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.order.model.OrderSnapshot;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberReceiptOwnershipTest {
    @Test
    void foreignAndMissingOrdersDoNotReadCheckoutDetails() {
        CartGateway cartGateway = mock(CartGateway.class);
    CheckoutAccountGateway checkoutAccountGateway = mock(CheckoutAccountGateway.class);
    CheckoutCartGateway checkoutCartGateway = mock(CheckoutCartGateway.class);
    CheckoutService checkoutService = mock(CheckoutService.class);
    CouponGateway couponGateway = mock(CouponGateway.class);
    CouponTemplateGateway couponTemplateGateway = mock(CouponTemplateGateway.class);
        OrderGateway orderPersistenceService = mock(OrderGateway.class);
        var query = new CheckoutReceiptQueryExe(checkoutService, orderPersistenceService);
        assertNull(query.execute(7L, "MISSING"));
        when(orderPersistenceService.findByOrderNo("FOREIGN"))
            .thenReturn(
                new OrderSnapshot(1L, "FOREIGN", 8L, BigDecimal.TEN, "PAID", List.of(), null));
        assertNull(query.execute(7L, "FOREIGN"));
        verifyNoInteractions(checkoutAccountGateway, checkoutCartGateway, cartGateway, couponTemplateGateway, couponGateway, checkoutService);
    }
}
