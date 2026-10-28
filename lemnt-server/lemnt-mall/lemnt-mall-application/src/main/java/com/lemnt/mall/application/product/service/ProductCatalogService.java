package com.lemnt.mall.application.product.service;

import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSkuSpecGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.gateway.StockReservationGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSpu;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

/** ProductCatalogService 跨表应用编排，保持原有事务和锁顺序。 */
@Component
@RequiredArgsConstructor
public class ProductCatalogService {
    private final ProductSpuGateway productSpuGateway;
    private final ProductSkuGateway productSkuGateway;
    private final ProductSkuSpecGateway productSkuSpecGateway;
    private final StockReservationGateway stockReservationGateway;

    /** 创建商品主体、SKU 和属性，任一步失败整体回滚。 */
    @Transactional(rollbackFor = Exception.class)
    public ProductSpu createSpu(ProductSpu value) {
        var saved = productSpuGateway.insertSpu(value);
        for (var sku : value.skus()
        ) {
            var child = productSkuGateway.insertSku(new ProductSku(null,
                    saved.id(),
                    sku.code(),
                    sku.name(),
                    sku.price(),
                    sku.stock(),
                    sku.image(),
                    sku.properties(),
                    sku.status(), sku.weight(), sku.volume()));
            productSkuSpecGateway.saveProperties(child.id(), sku.properties());
        }
        return spu(saved.id());
    }

    /** 按 SPU 原子保存完整 SKU 集合，价格、库存和规格不再通过独立后台接口修改。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSpu(ProductSpu value) {
        var original = productSpuGateway.lockSpu(value.id());
        if (original == null || "DELETED".equals(original.status())
        ) {
            throw new IllegalArgumentException("商品不存在");
        }
        var validated = ProductCatalogValidation.newSpu(
                new ProductSpu(value.id(), original.code(), value.name(), value.categoryId(), value.brandId(),
                        value.description(), value.image(), original.status(), value.skus(), value.deliveryTypes(), value.deliveryTemplateId()));
        var existing = productSkuGateway.catalogSkus(value.id());
        // 整体保存时先锁定旧 SKU 并释放规格键，允许交换规格及替换旧规格。
        existing.stream().map(ProductSku::id).sorted().forEach(productSkuGateway::lockById);
        productSkuGateway.clearVariantKeys(value.id());
        var retained = new HashSet<Long>();
        for (var sku : validated.skus()
        ) {
            if (sku.id() == null
            ) {
                var saved = productSkuGateway.insertSku(new ProductSku(null, value.id(), sku.code(),
                        sku.name(), sku.price(), sku.stock(), sku.image(), sku.properties(), "ON_SHELF", sku.weight(), sku.volume()));
                productSkuSpecGateway.saveProperties(saved.id(), sku.properties());
            } else {
                var old = existing.stream().filter(item -> item.id().equals(sku.id())).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("SKU 不属于当前商品"));
                if (!retained.add(sku.id()) || !old.code().equals(sku.code())
                ) {
                    throw new IllegalArgumentException("SKU 编号重复或编码已被修改");
                }
                productSkuGateway.lockById(sku.id());
                if (!productSkuGateway.updateSku(new ProductSku(sku.id(), value.id(), sku.code(), sku.name(),
                        sku.price(), sku.stock(), sku.image(), sku.properties(), "ON_SHELF", sku.weight(), sku.volume()))
                ) {
                    throw new IllegalStateException("SKU 保存失败");
                }
                productSkuSpecGateway.saveProperties(sku.id(), sku.properties());
            }
        }
        for (var old : existing
        ) {
            if (!retained.contains(old.id())
            ) {
                productSkuGateway.lockById(old.id());
                if (stockReservationGateway.hasActive(old.id())
                ) {
                    throw new IllegalStateException("SKU仍有库存预占，不能移除");
                }
                productSkuGateway.deleteSku(old.id());
            }
        }
        return productSpuGateway.updateSpu(value);
    }

    /** 组合商品主体、SKU 和属性。 */
    @Transactional(readOnly = true)
    public ProductSpu spu(Long id) {
        var value = productSpuGateway.findSpu(id);
        if (value == null || "DELETED".equals(value.status())
        ) {
            return null;
        }
        var properties = productSkuSpecGateway.propertiesBySpu(id);
        var skus = productSkuGateway.catalogSkus(id).stream().map(sku -> withProperties(sku,
                properties.getOrDefault(sku.id(),
                Map.of()))).toList();
        return withSkus(value, skus);
    }

    /** 组合 SKU 主体和属性。 */
    @Transactional(readOnly = true)
    public ProductSku sku(Long id) {
        var value = productSkuGateway.catalogSku(id);
        return value == null ? null : withProperties(value, productSkuSpecGateway.properties(id));
    }

/** 小程序只展示当前可见、价格有效的商品与 SKU。 */
    @Transactional(readOnly = true)
    public ProductSpu product(Long id) {
        if (!productSpuGateway.visible(id)
        ) {
            return null;
        }
        var value = spu(id);
        return value == null ? null : withSkus(value,
                value.skus().stream().filter(sku -> "ON_SHELF".equals(sku.status()) && sku.price() != null && sku.price().signum() > 0).toList());
    }

    private ProductSpu withSkus(ProductSpu value, List<ProductSku> skus) {
        return new ProductSpu(value.id(),
                value.code(),
                value.name(),
                value.categoryId(),
                value.brandId(),
                value.description(),
                value.image(),
                value.status(),
                skus, value.deliveryTypes(), value.deliveryTemplateId());
    }

    private ProductSku withProperties(ProductSku value, Map<String, String> properties) {
        return new ProductSku(value.id(),
                value.spuId(),
                value.code(),
                value.name(),
                value.price(),
                value.stock(),
                value.image(),
                properties,
                value.status(), value.weight(), value.volume());
    }
}
