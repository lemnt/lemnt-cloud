package com.lemnt.trade.domain.gateway;

import com.lemnt.trade.domain.model.TradeOrder;
import java.time.LocalDateTime;
import java.util.List;

public interface TradeOrderGateway {
    LocalDateTime databaseNow();

    TradeOrder save(TradeOrder order);

    TradeOrder findByOrderNo(String orderNo);

    TradeOrder findByOrderNoForUpdate(String orderNo);

    boolean transitionStatus(String orderNo, String expectedStatus, String targetStatus);

    List<TradeOrder> findReservingCreatedBefore(LocalDateTime deadline, int limit);

    List<TradeOrder> findPendingPaymentCreatedBefore(LocalDateTime deadline, int limit);
}
