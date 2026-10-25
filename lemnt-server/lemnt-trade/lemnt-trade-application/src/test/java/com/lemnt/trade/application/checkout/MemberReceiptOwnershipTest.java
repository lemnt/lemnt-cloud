package com.lemnt.trade.application.checkout;

import com.lemnt.trade.application.checkout.executor.CheckoutReceiptQueryExe;
import com.lemnt.trade.domain.checkout.gateway.CheckoutGateway;
import com.lemnt.trade.domain.gateway.TradeOrderGateway;
import com.lemnt.trade.domain.model.TradeOrder;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberReceiptOwnershipTest {
    @Test
    void foreignAndMissingOrdersDoNotReadCheckoutDetails() {
        var checkout = mock(CheckoutGateway.class);
        var orders = mock(TradeOrderGateway.class);
        var query = new CheckoutReceiptQueryExe(checkout, orders);
        assertNull(query.execute(7L, "MISSING"));
        when(orders.findByOrderNo("FOREIGN"))
                .thenReturn(
                        new TradeOrder(1L, "FOREIGN", 8L, BigDecimal.TEN, "PAID", List.of(), null));
        assertNull(query.execute(7L, "FOREIGN"));
        verifyNoInteractions(checkout);
    }
}
