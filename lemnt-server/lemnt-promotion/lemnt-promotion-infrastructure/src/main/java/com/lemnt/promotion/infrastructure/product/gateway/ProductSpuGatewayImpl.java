package com.lemnt.promotion.infrastructure.product.gateway;

import com.lemnt.product.client.ProductCatalogClient;
import com.lemnt.product.client.dto.ProductSpuBriefDTO;
import com.lemnt.promotion.domain.product.gateway.ProductSpuGateway;
import com.lemnt.promotion.domain.product.model.ProductSpu;

import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

import java.util.*;

/** 仅映射活动实际使用的SPU字段，其余属性保持未知，不补造商品详情。 */
@Component
public class ProductSpuGatewayImpl implements ProductSpuGateway {
    @DubboReference(check = false, timeout = 5000, retries = 0)
    private ProductCatalogClient client;

    @Override
    public ProductSpu findSpu(Long id) {
        return from(client.findSpu(id));
    }

    @Override
    public List<ProductSpu> findSpus(List<Long> ids) {
        return ids == null || ids.isEmpty()
                ? List.of()
                : client.findSpus(ids).stream().map(this::from).toList();
    }

    @Override
    public List<Long> existingSpuIds(List<Long> ids) {
        return findSpus(ids).stream().map(ProductSpu::id).toList();
    }

    private ProductSpu from(ProductSpuBriefDTO v) {
        return v == null
                ? null
                : new ProductSpu(
                        v.id(),
                        null,
                        v.name(),
                        v.categoryId(),
                        null,
                        null,
                        v.image(),
                        v.status(),
                        List.of());
    }
}
