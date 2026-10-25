package com.lemnt.trade.domain.fulfillment.gateway;

import com.lemnt.trade.domain.fulfillment.model.*;
import java.time.LocalDateTime;

public interface FulfillmentGateway {
    boolean inventoryConfirmed(String orderNo);

    void confirmInventory(String orderNo, LocalDateTime time);

    boolean activeRefund(String orderNo);

    Shipment shipment(String orderNo);

    void saveShipment(Shipment shipment);

    boolean receive(String orderNo, LocalDateTime time);

    OrderPage orders(FulfillmentFilter filter);

    RefundPage refunds(FulfillmentFilter filter);

    RefundRequest refund(String refundNo);

    RefundRequest lockRefund(String refundNo);

    RefundRequest findRefundRequest(String orderNo, String requestNo);

    void saveRefundRequest(RefundRequest request);

    boolean rejectRefund(String refundNo, String reason, LocalDateTime time);
}
