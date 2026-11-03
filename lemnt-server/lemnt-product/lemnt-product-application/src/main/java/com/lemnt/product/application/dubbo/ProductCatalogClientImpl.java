package com.lemnt.product.application.dubbo;

import com.lemnt.product.application.product.service.ProductInventoryService;
import com.lemnt.product.client.ProductCatalogClient;
import com.lemnt.product.client.dto.ProductSnapshotDTO;
import com.lemnt.product.domain.product.gateway.ProductCatalogGateway;
import com.lemnt.product.domain.product.model.ProductSnapshot;

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
    private final com.lemnt.product.domain.product.gateway.ProductSkuGateway skus;
    private final com.lemnt.product.domain.product.gateway.ProductSpuGateway spus;

    @Override
    public com.lemnt.product.client.dto.ProductSkuBriefDTO catalogSku(Long id) {
        return sku(skus.catalogSku(id));
    }
    @Override
    public List<com.lemnt.product.client.dto.ProductSkuBriefDTO> catalogSkus(Long spuId) {
        return skus.catalogSkus(spuId).stream().map(this::sku).toList();
    }
    @Override
    public List<com.lemnt.product.client.dto.ProductSkuBriefDTO> skusBySpuIds(List<Long> ids) {
        return skus.skusBySpuIds(ids).stream().map(this::sku).toList();
    }
    @Override
    public com.lemnt.product.client.dto.ProductSpuBriefDTO findSpu(Long id) {
        return spu(spus.findSpu(id));
    }
    @Override
    public List<com.lemnt.product.client.dto.ProductSpuBriefDTO> findSpus(List<Long> ids) {
        return spus.findSpus(ids).stream().map(this::spu).toList();
    }
    private com.lemnt.product.client.dto.ProductSpuBriefDTO spu(
            com.lemnt.product.domain.product.model.ProductSpu value) {
        return value == null ? null : new com.lemnt.product.client.dto.ProductSpuBriefDTO(
                value.id(), value.name(), value.categoryId(), value.image(), value.status());
    }
    private com.lemnt.product.client.dto.ProductSkuBriefDTO sku(
            com.lemnt.product.domain.product.model.ProductSku value) {
        return value == null ? null : new com.lemnt.product.client.dto.ProductSkuBriefDTO(
                value.id(), value.spuId(), value.code(), value.name(), value.price(), value.stock(),
                value.image(), value.specs().stream().map(spec -> new ProductSnapshotDTO.Spec(
                        spec.specId(), spec.specName(), spec.specValueId(), spec.specValueName())).toList(),
                value.status(), value.weight(), value.volume());
    }

    @Override
    public com.lemnt.product.client.dto.StockReservationDTO reservationStatus(
            com.lemnt.product.client.bo.StockReservationQueryBo query) {
        if (query == null) throw new IllegalArgumentException("预占查询条件不能为空");
        var value = productInventoryService.reservationStatus(query.reservationNo(), query.skuId());
        return value == null
                ? null
                : new com.lemnt.product.client.dto.StockReservationDTO(
                        value.reservationNo(), value.productId(), value.quantity(), value.status());
    }

    @Override
    public void returnStock(String orderNo, String afterSaleNo, Long skuId, Integer quantity) {
        productInventoryService.returnStock(orderNo, afterSaleNo, skuId, quantity);
    }

    @Override
    public ProductSnapshotDTO findOnShelf(Long id) {
        return toDTO(productCatalogGateway.findOnShelf(id));
    }

    @Override
    public List<ProductSnapshotDTO> findOnShelfBatch(List<Long> ids) {
        var values = productCatalogGateway.findOnShelfBatch(ids);
        return ids.stream()
                .map(values::get)
                .filter(java.util.Objects::nonNull)
                .map(this::toDTO)
                .toList();
    }

    /** 远程输出与结算读取相同的可信目录快照。 */
    private ProductSnapshotDTO toDTO(ProductSnapshot value) {
        return value == null
                ? null
                : new ProductSnapshotDTO(
                        value.id(),
                        value.productCode(),
                        value.name(),
                        value.price(),
                        value.stock(),
                        value.spuId(),
                        value.spuName(),
                        value.specs() == null
                                ? null
                                : value.specs().stream()
                                        .map(
                                                snapshotProperty ->
                                                        new ProductSnapshotDTO.Spec(
                                                                snapshotProperty.specId(),
                                                                snapshotProperty.specName(),
                                                                snapshotProperty.specValueId(),
                                                                snapshotProperty.specValueName()))
                                        .toList(),
                        value.picUrl(), value.deliveryTypes(), value.deliveryTemplateId(),
                        value.weight(), value.volume());
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
