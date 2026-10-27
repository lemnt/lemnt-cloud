package com.lemnt.mall.infrastructure.catalog.gateway;

import com.lemnt.mall.domain.catalog.gateway.ProductCatalogGateway;
import com.lemnt.mall.domain.catalog.model.ProductSnapshot;
import com.lemnt.mall.domain.product.gateway.ProductGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 商城结算读取本地可售商品，不通过 Dubbo 调用自身。 */
@Component
@RequiredArgsConstructor
public class ProductCatalogGatewayImpl implements ProductCatalogGateway {
    private final ProductGateway productGateway;

    @Override
    public ProductSnapshot findOnShelf(Long id) {
        var value = productGateway.findSellableById(id);
        return value == null ? null : new ProductSnapshot(value.id(), value.productCode(),
                value.name(), value.price(), value.stock());
    }

    @Override
    public Map<Long, ProductSnapshot> findOnShelfBatch(List<Long> ids) {
        return productGateway.findSellableByIds(ids).stream().collect(Collectors.toMap(
                value -> value.id(), value -> new ProductSnapshot(value.id(), value.productCode(),
                        value.name(), value.price(), value.stock())));
    }
}
