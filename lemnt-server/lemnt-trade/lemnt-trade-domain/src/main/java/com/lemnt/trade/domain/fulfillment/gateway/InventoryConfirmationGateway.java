package com.lemnt.trade.domain.fulfillment.gateway;

import java.time.LocalDateTime;

/** InventoryConfirmation 表领域存储接口。 */
public interface InventoryConfirmationGateway {
    /**
     * 库存已确认。
     */
    boolean inventoryConfirmed(String orderNo);

    /**
     * 确认库存。
     */
    void confirmInventory(String orderNo, LocalDateTime time);
}
