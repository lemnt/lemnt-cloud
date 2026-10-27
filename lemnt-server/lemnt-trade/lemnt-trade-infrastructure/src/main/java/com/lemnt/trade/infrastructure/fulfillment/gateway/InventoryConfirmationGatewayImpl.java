package com.lemnt.trade.infrastructure.fulfillment.gateway;

import com.lemnt.trade.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.trade.infrastructure.fulfillment.mapper.InventoryConfirmationMapper;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/** InventoryConfirmation 表持久化实现。 */
@Component
@RequiredArgsConstructor
public class InventoryConfirmationGatewayImpl implements InventoryConfirmationGateway {
    private final InventoryConfirmationMapper inventoryConfirmationMapper;

    @Override
    /**
     * 库存已确认。
     */
    public boolean inventoryConfirmed(String no) {
        return inventoryConfirmationMapper.confirmed(no) == 1;
    }

    @Override
    /**
     * 确认库存。
     */
    public void confirmInventory(String no, LocalDateTime time) {
        inventoryConfirmationMapper.confirm(no, time);
    }
}
