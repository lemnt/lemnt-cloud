package com.lemnt.trade.infrastructure.fulfillment.gateway;

import com.lemnt.trade.domain.fulfillment.gateway.InventoryConfirmationGateway;
import com.lemnt.trade.infrastructure.fulfillment.dataobject.InventoryConfirmationDO;
import com.lemnt.trade.infrastructure.fulfillment.mapper.InventoryConfirmationMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
        return confirmedRow(no) == 1;
    }

    @Override
    /**
     * 确认库存。
     */
    public void confirmInventory(String no, LocalDateTime time) {
        inventoryConfirmationMapper.confirm(no, time);
    }

    /**
     * 已确认。
     */
    private int confirmedRow(String orderNo) {
        return inventoryConfirmationMapper
                .selectCount(
                        Wrappers.<InventoryConfirmationDO>lambdaQuery()
                                .eq(InventoryConfirmationDO::getOrderNo, orderNo))
                .intValue();
    }
}
