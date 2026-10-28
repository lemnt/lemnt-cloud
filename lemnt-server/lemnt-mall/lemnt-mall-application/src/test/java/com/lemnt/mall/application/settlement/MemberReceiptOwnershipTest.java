package com.lemnt.mall.application.settlement;

import com.lemnt.mall.application.settlement.executor.SettlementReceiptQueryExe;
import com.lemnt.mall.application.settlement.service.SettlementRecordService;
import com.lemnt.mall.domain.cart.gateway.CartGateway;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.member.gateway.MemberLockGateway;
import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.gateway.SettlementGateway;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberReceiptOwnershipTest {
    @Test
    void foreignAndMissingOrdersDoNotReadCheckoutDetails() {
        CartGateway cartGateway = mock(CartGateway.class);
    MemberLockGateway checkoutAccountGateway = mock(MemberLockGateway.class);
    SettlementGateway checkoutCartGateway = mock(SettlementGateway.class);
    SettlementRecordService checkoutService = mock(SettlementRecordService.class);
    CouponGateway couponGateway = mock(CouponGateway.class);
    CouponTemplateGateway couponTemplateGateway = mock(CouponTemplateGateway.class);
        OrderGateway orderPersistenceService = mock(OrderGateway.class);
        var query = new SettlementReceiptQueryExe(checkoutService, orderPersistenceService);
        assertNull(query.execute(7L, "MISSING"));
        when(orderPersistenceService.findByOrderNo("FOREIGN"))
            .thenReturn(
                new SettlementOrder(1L, "FOREIGN", 8L, BigDecimal.TEN, "PAID", List.of(), null));
        assertNull(query.execute(7L, "FOREIGN"));
        verifyNoInteractions(checkoutAccountGateway, checkoutCartGateway, cartGateway, couponTemplateGateway, couponGateway, checkoutService);
    }
}
