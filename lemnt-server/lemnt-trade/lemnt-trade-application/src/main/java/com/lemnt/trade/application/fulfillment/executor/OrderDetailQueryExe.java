package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.converter.FulfillmentDTOConverter;
import com.lemnt.trade.application.fulfillment.dto.OrderDetailDTO;
import com.lemnt.trade.application.order.service.OrderPersistenceService;
import com.lemnt.trade.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.trade.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.trade.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.trade.domain.order.model.OrderRules;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 订单详情查询执行器。
 */
@Component
@RequiredArgsConstructor
public class OrderDetailQueryExe {
    private final InventoryConfirmationGateway inventoryConfirmationGateway;
    private final OrderPersistenceService orderPersistenceService;
    private final AfterSaleGateway refundRequestGateway;
    private final ShipmentGateway shipmentGateway;

    /**
     * 执行订单详情查询。
     */
    @Transactional(readOnly = true)
    public OrderDetailDTO execute(String value) {
        String no = OrderRules.text(value, 40);
        var order = orderPersistenceService.findByOrderNo(no);
        return order == null
                ? null
                : new OrderDetailDTO(
                no,
                order.memberId(),
                order.totalAmount(),
                order.status(),
                inventoryConfirmationGateway.inventoryConfirmed(no),
                refundRequestGateway.activeRefund(no),
                order.items().stream()
                .map(
                item -> new com.lemnt.trade.application.fulfillment.dto.OrderItemDTO(item.productId(),
                item.productCode(),
                item.productName(),
                item.unitPrice(),
                item.quantity(),
                item.lineAmount(),
                item.spuId(),
                item.spuName(),
                item.properties() == null ? null : item.properties().stream().map(snapshotProperty ->
                        new com.lemnt.trade.application.fulfillment.dto.OrderItemDTO.Property(snapshotProperty.propertyId(), snapshotProperty.propertyName(),
                                snapshotProperty.valueId(), snapshotProperty.valueName())).toList(),
                item.picUrl()))
                .toList(),
                FulfillmentDTOConverter.shipment(shipmentGateway.shipment(no)));
    }
}
