package com.lemnt.mall.application.dubbo;

import com.lemnt.mall.application.product.service.ProductInventoryService;
import com.lemnt.mall.client.ProductCatalogClient;
import com.lemnt.mall.client.dto.ProductSnapshotDTO;
import com.lemnt.mall.domain.product.gateway.ProductCatalogGateway;
import com.lemnt.mall.domain.product.model.ProductSnapshot;

import org.apache.dubbo.config.annotation.DubboService;

import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 商品目录客户端实现。
 */
@DubboService
@RequiredArgsConstructor
public class ProductCatalogClientImpl implements ProductCatalogClient {
    private final ProductCatalogGateway productCatalogGateway;
    private final ProductInventoryService productInventoryService;

    @Override
    public ProductSnapshotDTO findOnShelf(Long id) {
        return toDTO(productCatalogGateway.findOnShelf(id));
    }

    @Override
    public List<ProductSnapshotDTO> findOnShelfBatch(List<Long> ids) {
        var values = productCatalogGateway.findOnShelfBatch(ids);
        return ids.stream().map(values::get).filter(java.util.Objects::nonNull).map(this::toDTO).toList();
    }

    /** 远程输出与结算读取相同的可信目录快照。 */
    private ProductSnapshotDTO toDTO(ProductSnapshot value) {
        return value == null ? null : new ProductSnapshotDTO(value.id(), value.productCode(), value.name(), value.price(),
                value.stock(), value.spuId(), value.spuName(), value.properties(), value.picUrl());
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
