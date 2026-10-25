package com.lemnt.trade.infrastructure.fulfillment.gateway;

import com.lemnt.trade.domain.fulfillment.gateway.FulfillmentGateway;
import com.lemnt.trade.domain.fulfillment.model.*;
import com.lemnt.trade.infrastructure.fulfillment.converter.FulfillmentConverter;
import com.lemnt.trade.infrastructure.fulfillment.mapper.FulfillmentMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class FulfillmentGatewayImpl implements FulfillmentGateway {
    private final FulfillmentMapper mapper;

    public boolean inventoryConfirmed(String no) {
        return mapper.confirmed(no) == 1;
    }

    public void confirmInventory(String no, LocalDateTime time) {
        mapper.confirm(no, time);
    }

    public boolean activeRefund(String no) {
        return mapper.activeRefund(no) > 0;
    }

    public Shipment shipment(String no) {
        return FulfillmentConverter.shipment(mapper.shipment(no));
    }

    public void saveShipment(Shipment value) {
        mapper.insertShipment(FulfillmentConverter.shipment(value));
    }

    public boolean receive(String no, LocalDateTime time) {
        return mapper.receive(no, time) == 1;
    }

    @Transactional(readOnly = true)
    public OrderPage orders(FulfillmentFilter filter) {
        return new OrderPage(
                mapper.orders(filter).stream().map(FulfillmentConverter::orderSummary).toList(),
                mapper.orderCount(filter));
    }

    @Transactional(readOnly = true)
    public RefundPage refunds(FulfillmentFilter filter) {
        return new RefundPage(
                mapper.refunds(filter).stream().map(FulfillmentConverter::refundRequest).toList(),
                mapper.refundCount(filter));
    }

    public RefundRequest refund(String no) {
        return FulfillmentConverter.refundRequest(mapper.refund(no));
    }

    public RefundRequest lockRefund(String no) {
        return FulfillmentConverter.refundRequest(mapper.lockRefund(no));
    }

    public RefundRequest findRefundRequest(String orderNo, String key) {
        return FulfillmentConverter.refundRequest(mapper.findRequest(orderNo, key));
    }

    public void saveRefundRequest(RefundRequest value) {
        mapper.insertRefund(FulfillmentConverter.refundRequest(value));
    }

    public boolean rejectRefund(String no, String reason, LocalDateTime time) {
        return mapper.reject(no, reason, time) == 1;
    }
}
