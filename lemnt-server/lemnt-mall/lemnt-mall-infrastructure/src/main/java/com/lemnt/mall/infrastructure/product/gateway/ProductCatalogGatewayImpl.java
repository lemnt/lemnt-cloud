package com.lemnt.mall.infrastructure.product.gateway;

import com.lemnt.mall.domain.product.gateway.ProductSkuSpecGateway;

import com.lemnt.mall.domain.product.gateway.ProductCatalogGateway;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.ProductSnapshot;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/** 商城结算读取本地可售商品，不通过 Dubbo 调用自身。 */
@Component
@RequiredArgsConstructor
public class ProductCatalogGatewayImpl implements ProductCatalogGateway {
    private final ProductSkuGateway productSkuGateway;

    private final ProductSpuGateway productSpuGateway;
    private final ProductSkuSpecGateway productSkuSpecGateway;

    @Override
    public ProductSnapshot findOnShelf(Long id) {
        return findOnShelfBatch(List.of(id)).get(id);
    }

    @Override
    public Map<Long, ProductSnapshot> findOnShelfBatch(List<Long> ids) {
        var sellable = productSkuGateway.findSellableByIds(ids);
        var properties = productSkuSpecGateway.specsBySkuIds(ids);
        var skus = productSkuGateway.byIds(ids).stream().collect(Collectors.toMap(row -> row.id(), row -> row));
        var spuIds = skus.values().stream().map(row -> row.spuId()).filter(Objects::nonNull).distinct().toList();
        var spus = productSpuGateway.findSpus(spuIds).stream().collect(Collectors.toMap(row -> row.id(), row -> row));
        return sellable.stream().collect(Collectors.toMap(value -> value.id(), value -> {
            var sku = skus.get(value.id());
            var spu = sku == null ? null : spus.get(sku.spuId());
            return new ProductSnapshot(value.id(), value.productCode(), value.name(), value.price(), value.stock(),
                    spu == null ? List.of() : spu.deliveryTypes(), spu == null ? null : spu.deliveryTemplateId(),
                    sku == null ? null : sku.weight(), sku == null ? null : sku.volume(),
                    sku == null ? null : sku.spuId(), spu == null ? null : spu.name(),
                properties.getOrDefault(value.id(), List.of()).stream()
                            .sorted(java.util.Comparator.comparing(row -> row.propertyName())).toList() == null ? null : properties.getOrDefault(value.id(), List.of()).stream()
                            .sorted(java.util.Comparator.comparing(row -> row.propertyName())).toList().stream().map(snapshotProperty ->
                        new ProductSnapshot.Property(snapshotProperty.propertyId(), snapshotProperty.propertyName(),
                                snapshotProperty.valueId(), snapshotProperty.valueName())).toList(),
                    sku != null && sku.image() != null && !sku.image().isBlank()
                            ? sku.image() : spu == null ? null : spu.image());
        }));
    }
}
