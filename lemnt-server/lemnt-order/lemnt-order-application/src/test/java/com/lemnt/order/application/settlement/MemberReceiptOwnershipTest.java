package com.lemnt.order.application.settlement;

import com.lemnt.order.application.settlement.executor.SettlementReceiptQueryExe;
import com.lemnt.order.application.settlement.service.SettlementOrderService;
import com.lemnt.order.application.settlement.service.SettlementRecordService;
import com.lemnt.order.domain.cart.gateway.CartSnapshotGateway;
import com.lemnt.order.domain.member.gateway.MemberLockGateway;
import com.lemnt.order.domain.settlement.gateway.SettlementGateway;
import com.lemnt.order.domain.settlement.model.SettlementOrder;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberReceiptOwnershipTest {
    @Test
    void foreignAndMissingOrdersDoNotReadCheckoutDetails() {
        CartSnapshotGateway cartGateway = mock(CartSnapshotGateway.class);
        MemberLockGateway checkoutAccountGateway = mock(MemberLockGateway.class);
        SettlementGateway checkoutCartSnapshotGateway = mock(SettlementGateway.class);
        SettlementRecordService checkoutService = mock(SettlementRecordService.class);
        SettlementOrderService orderPersistenceService = mock(SettlementOrderService.class);
        var query = new SettlementReceiptQueryExe(checkoutService, orderPersistenceService);
        assertNull(query.execute(7L, "MISSING"));
        when(orderPersistenceService.findByOrderNo("FOREIGN"))
            .thenReturn(
                new SettlementOrder(
                    1L, "FOREIGN", 8L, BigDecimal.TEN, "PAID", List.of(), null));
        assertNull(query.execute(7L, "FOREIGN"));
        verifyNoInteractions(
            checkoutAccountGateway, checkoutCartSnapshotGateway, cartGateway, checkoutService);
    }
}
