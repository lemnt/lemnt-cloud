package com.lemnt.order.application.fulfillment.executor;

import com.lemnt.order.application.fulfillment.converter.FulfillmentDTOConverter;
import com.lemnt.order.application.fulfillment.dto.OrderDetailDTO;
import com.lemnt.order.application.fulfillment.dto.OrderItemDTO;
import com.lemnt.order.application.order.service.OrderPersistenceService;
import com.lemnt.order.domain.aftersale.gateway.AfterSaleGateway;
import com.lemnt.order.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.order.domain.fulfillment.gateway.ShipmentGateway;
import com.lemnt.order.domain.order.model.OrderRules;

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
                                        item ->
                                                new OrderItemDTO(
                                                        item.productId(),
                                                        item.productCode(),
                                                        item.productName(),
                                                        item.unitPrice(),
                                                        item.quantity(),
                                                        item.lineAmount(),
                                                        item.spuId(),
                                                        item.spuName(),
                                                        item.specs() == null
                                                                ? null
                                                                : item.specs().stream()
                                                                        .map(
                                                                                snapshotProperty ->
                                                                                        new OrderItemDTO
                                                                                                .Spec(
                                                                                                snapshotProperty
                                                                                                        .specId(),
                                                                                                snapshotProperty
                                                                                                        .specName(),
                                                                                                snapshotProperty
                                                                                                        .specValueId(),
                                                                                                snapshotProperty
                                                                                                        .specValueName()))
                                                                        .toList(),
                                                        item.picUrl(),
                                                        item.orderItemId(),
                                                        item.commentStatus()))
                                .toList(),
                        FulfillmentDTOConverter.shipment(shipmentGateway.shipment(no)),
                        order.commentStatus(),
                        order.finishTime());
    }
}
