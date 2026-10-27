package com.lemnt.mall.application.dubbo;

import com.lemnt.mall.application.product.service.ProductInventoryService;
import com.lemnt.mall.client.ProductCatalogClient;
import com.lemnt.mall.client.dto.ProductSnapshotDTO;
import com.lemnt.mall.domain.product.gateway.ProductGateway;
import com.lemnt.mall.domain.product.model.Product;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 商品目录客户端实现。
 */
@DubboService
@RequiredArgsConstructor
public class ProductCatalogClientImpl implements ProductCatalogClient {
    private final ProductGateway productGateway;
    private final ProductInventoryService productInventoryService;

    @Override
    public ProductSnapshotDTO findOnShelf(Long id) {
        Product p = productGateway.findSellableById(id);
        return p == null || !"ON_SHELF".equals(p.status())
                ? null
                : new ProductSnapshotDTO(p.id(), p.productCode(), p.name(), p.price(), p.stock());
    }

    @Override
    public List<ProductSnapshotDTO> findOnShelfBatch(List<Long> ids) {
        return productGateway.findSellableByIds(ids).stream()
                .map(
                p ->
                new ProductSnapshotDTO(
                p.id(), p.productCode(), p.name(), p.price(), p.stock()))
                .toList();
    }

    @Override
    public void cancelReservation(String reservationNo, Long id, Integer quantity) {
        productInventoryService.cancelReservation(reservationNo, id, quantity);
    }

    @Override
    public boolean reserveStock(String reservationNo, Long id, Integer quantity) {
        return productInventoryService.reserveStock(reservationNo, id, quantity);
    }

    @Override
    public void releaseStock(String reservationNo, Long id) {
        productInventoryService.releaseStock(reservationNo, id);
    }

    @Override
    public void confirmStock(String reservationNo, Long id) {
        productInventoryService.confirmStock(reservationNo, id);
    }
}
