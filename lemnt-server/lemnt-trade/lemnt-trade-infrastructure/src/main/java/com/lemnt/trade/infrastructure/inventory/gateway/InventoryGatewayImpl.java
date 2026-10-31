package com.lemnt.trade.infrastructure.inventory.gateway;

import com.lemnt.mall.client.ProductCatalogClient;
import com.lemnt.trade.domain.inventory.gateway.InventoryGateway;

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
