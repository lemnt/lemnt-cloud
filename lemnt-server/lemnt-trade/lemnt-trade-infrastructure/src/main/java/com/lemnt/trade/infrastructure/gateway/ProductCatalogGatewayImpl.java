package com.lemnt.trade.infrastructure.gateway;

import com.lemnt.mall.client.ProductCatalogClient;
import com.lemnt.mall.client.dto.ProductSnapshotDTO;
import com.lemnt.trade.domain.gateway.ProductCatalogGateway;
import com.lemnt.trade.domain.model.ProductSnapshot;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductCatalogGatewayImpl implements ProductCatalogGateway {
    @DubboReference(check = false)
    private ProductCatalogClient client;

    @Override
    public ProductSnapshot findOnShelf(Long productId) {
        ProductSnapshotDTO p = client.findOnShelf(productId);
        return p == null ? null : new ProductSnapshot(p.id(), p.productCode(), p.name(), p.price(), p.stock());
    }

    @Override
    public boolean reserveStock(String reservationNo, Long productId, Integer quantity) {
        return client.reserveStock(reservationNo, productId, quantity);
    }

    @Override
    public void releaseStock(String reservationNo, Long productId) {
        client.releaseStock(reservationNo, productId);
    }

    @Override
    public void confirmStock(String reservationNo, Long productId) {
        client.confirmStock(reservationNo, productId);
    }
}