package com.lemnt.mall.application.dubbo;

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
        Product p = gateway.findSellableById(id);
        return p == null || !"ON_SHELF".equals(p.status())
                ? null
                : new ProductSnapshotDTO(p.id(), p.productCode(), p.name(), p.price(), p.stock());
    }

    @Override
    public java.util.List<ProductSnapshotDTO> findOnShelfBatch(java.util.List<Long> ids) {
        return gateway.findSellableByIds(ids).stream()
                .map(
                        p ->
                                new ProductSnapshotDTO(
                                        p.id(), p.productCode(), p.name(), p.price(), p.stock()))
                .toList();
    }

    @Override
    public void cancelReservation(String reservationNo, Long id, Integer quantity) {
        gateway.cancelReservation(reservationNo, id, quantity);
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
