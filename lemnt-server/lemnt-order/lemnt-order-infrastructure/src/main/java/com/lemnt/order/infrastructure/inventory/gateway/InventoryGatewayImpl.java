package com.lemnt.order.infrastructure.inventory.gateway;

import com.lemnt.order.domain.inventory.gateway.InventoryGateway;
import com.lemnt.product.client.ProductCatalogClient;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 商城库存同步远程接口实现。
 */
@Component
@RequiredArgsConstructor
public class InventoryGatewayImpl implements InventoryGateway {
    @DubboReference(check = false, timeout = 3000, retries = 0)
    private ProductCatalogClient client;

    @Override
    public boolean reserveStock(String reservationNo, Long productId, Integer quantity) {
        return client.reserveStock(reservationNo, productId, quantity);
    }

    @Override
    public void confirmStock(String reservationNo, Long productId) {
        client.confirmStock(reservationNo, productId);
    }
}
