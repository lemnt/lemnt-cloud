package com.lemnt.mall.adapter.rpc;

import com.lemnt.mall.client.ProductCatalogClient;
import com.lemnt.mall.client.dto.ProductSnapshotDTO;
import com.lemnt.mall.domain.gateway.ProductGateway;
import com.lemnt.mall.domain.model.Product;
import lombok.RequiredArgsConstructor;
import org.apache.dubbo.config.annotation.DubboService;

@DubboService
@RequiredArgsConstructor
public class ProductCatalogClientImpl implements ProductCatalogClient {
    private final ProductGateway gateway;

    @Override
    public ProductSnapshotDTO findOnShelf(Long id) {
        Product p = gateway.findById(id);
        return p == null || !"ON_SHELF".equals(p.status()) ? null
                : new ProductSnapshotDTO(p.id(), p.productCode(), p.name(), p.price(), p.stock());
    }

    @Override
    public boolean reserveStock(String reservationNo, Long id, Integer quantity) {
        return gateway.reserveStock(reservationNo, id, quantity);
    }

    @Override
    public void releaseStock(String reservationNo, Long id) {
        gateway.releaseStock(reservationNo, id);
    }

    @Override
    public void confirmStock(String reservationNo, Long id) {
        gateway.confirmStock(reservationNo, id);
    }
}