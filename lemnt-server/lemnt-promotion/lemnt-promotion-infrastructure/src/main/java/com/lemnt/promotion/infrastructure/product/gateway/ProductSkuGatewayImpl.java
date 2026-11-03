package com.lemnt.promotion.infrastructure.product.gateway;

import com.lemnt.product.client.ProductCatalogClient;
import com.lemnt.product.client.dto.ProductSkuBriefDTO;
import com.lemnt.promotion.domain.product.gateway.ProductSkuGateway;
import com.lemnt.promotion.domain.product.model.ProductSku;
import com.lemnt.promotion.domain.product.model.ProductSkuSpec;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.*;

/** 活动配置读取当前SKU，不持有商品库行锁。 */
@Component
public class ProductSkuGatewayImpl implements ProductSkuGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private ProductCatalogClient client;

    @Override
    public ProductSku catalogSku(Long id) {
        return from(client.catalogSku(id));
    }

    @Override
    public List<ProductSku> catalogSkus(Long id) {
        return client.catalogSkus(id).stream().map(this::from).toList();
    }

    @Override
    public List<ProductSku> skusBySpuIds(List<Long> ids) {
        return ids == null || ids.isEmpty()
                ? List.of()
                : client.skusBySpuIds(ids).stream().map(this::from).toList();
    }

    private ProductSku from(ProductSkuBriefDTO v) {
        return v == null
                ? null
                : new ProductSku(
                        v.id(),
                        v.spuId(),
                        v.code(),
                        v.name(),
                        v.price(),
                        v.stock(),
                        v.image(),
                        v.specs() == null
                                ? List.of()
                                : v.specs().stream()
                                        .map(
                                                s ->
                                                        new ProductSkuSpec(
                                                                s.specId(),
                                                                s.specName(),
                                                                s.specValueId(),
                                                                s.specValueName()))
                                        .toList(),
                        v.status(),
                        v.weight(),
                        v.volume());
    }
}
