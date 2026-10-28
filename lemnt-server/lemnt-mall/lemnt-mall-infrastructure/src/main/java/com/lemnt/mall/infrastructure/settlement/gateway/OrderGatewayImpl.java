package com.lemnt.mall.infrastructure.settlement.gateway;

import com.lemnt.mall.domain.settlement.gateway.OrderGateway;
import com.lemnt.mall.domain.settlement.model.SettlementDelivery;
import com.lemnt.mall.domain.settlement.model.SettlementItem;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;
import com.lemnt.trade.client.TradeOrderClient;
import com.lemnt.trade.client.bo.OrderCreateBo;
import com.lemnt.trade.client.dto.OrderDTO;
import com.lemnt.trade.client.dto.OrderDeliveryDTO;
import com.lemnt.trade.client.dto.OrderItemDTO;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/** 只通过交易服务契约读写订单，不共享事务或 Mapper。 */
@Component
public class OrderGatewayImpl implements OrderGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private TradeOrderClient client;

    @Override
    public SettlementOrder save(SettlementOrder order) {
        return from(client.create(new OrderCreateBo(order.orderNo(), order.memberId(),
                order.totalAmount(), order.items().stream().map(item ->
                        new OrderItemDTO(item.productId(),
                item.productCode(),
                item.productName(),
                item.unitPrice(),
                item.quantity(),
                item.lineAmount(),
                item.payAmount(),
                item.deliveryPrice(),
                item.spuId(),
                item.spuName(),
                item.properties() == null ? null : item.properties().stream().map(snapshotProperty ->
                        new OrderItemDTO.Property(snapshotProperty.propertyId(), snapshotProperty.propertyName(),
                                snapshotProperty.valueId(), snapshotProperty.valueName())).toList(),
                item.picUrl())).toList(), order.usePoint(), order.givePoint(), toDelivery(order.delivery()))));
    }

    @Override
    public SettlementOrder findByOrderNo(String orderNo) {
        return from(client.getByOrderNo(orderNo));
    }

    private SettlementOrder from(OrderDTO value) {
        return value == null ? null : new SettlementOrder(value.id(), value.orderNo(), value.memberId(),
                value.totalAmount(), value.status(), value.items().stream().map(item ->
                        new SettlementItem(item.productId(),
                item.productCode(),
                item.productName(),
                item.unitPrice(),
                item.quantity(),
                item.lineAmount(),
                item.payAmount(),
                item.deliveryPrice(),
                item.spuId(),
                item.spuName(),
                item.properties() == null ? null : item.properties().stream().map(snapshotProperty ->
                        new SettlementItem.Property(snapshotProperty.propertyId(), snapshotProperty.propertyName(),
                                snapshotProperty.valueId(), snapshotProperty.valueName())).toList(),
                item.picUrl())).toList(), value.createTime(), value.usePoint(), value.givePoint(), fromDelivery(value.delivery()));
    }

    private OrderDeliveryDTO toDelivery(
            SettlementDelivery value
    ) {
        return value == null ? null : new OrderDeliveryDTO(value.addressId(),
                value.deliveryType(), value.receiverName(), value.receiverMobile(), value.receiverAreaId(),
                value.receiverDetailAddress(), value.deliveryPrice());
    }

    private SettlementDelivery fromDelivery(
            OrderDeliveryDTO value
    ) {
        return value == null ? null : new SettlementDelivery(value.addressId(),
                value.deliveryType(), value.receiverName(), value.receiverMobile(), value.receiverAreaId(),
                value.receiverDetailAddress(), value.deliveryPrice());
    }
}
