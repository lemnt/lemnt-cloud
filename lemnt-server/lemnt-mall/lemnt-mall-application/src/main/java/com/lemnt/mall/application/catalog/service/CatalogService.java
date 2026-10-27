package com.lemnt.mall.application.catalog.service;

import com.lemnt.mall.domain.catalog.gateway.SkuPropertyGateway;
import com.lemnt.mall.domain.catalog.gateway.SpuGateway;
import com.lemnt.mall.domain.catalog.gateway.StockAdjustmentGateway;
import com.lemnt.mall.domain.catalog.model.CatalogSku;
import com.lemnt.mall.domain.catalog.model.CatalogSpu;
import com.lemnt.mall.domain.catalog.model.StockAdjustment;
import com.lemnt.mall.domain.product.gateway.ProductGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

/** CatalogService 跨表应用编排，保持原有事务和锁顺序。 */
@Component
@RequiredArgsConstructor
public class CatalogService {
    private final SpuGateway spuGateway;
    private final ProductGateway productGateway;
    private final SkuPropertyGateway skuPropertyGateway;
    private final StockAdjustmentGateway stockAdjustmentGateway;

    /** 创建商品主体、SKU 和属性，任一步失败整体回滚。 */
    @Transactional(rollbackFor = Exception.class)
    public CatalogSpu createSpu(CatalogSpu value) {
        var saved = spuGateway.insertSpu(value);
        for (var sku : value.skus()) {
            var child = productGateway.insertSku(new CatalogSku(null,
                    saved.id(),
                    sku.code(),
                    sku.name(),
                    sku.price(),
                    sku.stock(),
                    sku.image(),
                    sku.properties(),
                    sku.status()));
            skuPropertyGateway.saveProperties(child.id(), sku.properties());
        }
        return spu(saved.id());
    }

    /** 组合商品主体、SKU 和属性。 */
    @Transactional(readOnly = true)
    public CatalogSpu spu(Long id) {
        var value = spuGateway.findSpu(id);
        if (value == null) {
            return null;
        }
        var properties = skuPropertyGateway.propertiesBySpu(id);
        var skus = productGateway.catalogSkus(id).stream().map(sku -> withProperties(sku,
                properties.getOrDefault(sku.id(),
                Map.of()))).toList();
        return withSkus(value, skus);
    }

    /** 组合 SKU 主体和属性。 */
    @Transactional(readOnly = true)
    public CatalogSku sku(Long id) {
        var value = productGateway.catalogSku(id);
        return value == null ? null : withProperties(value, skuPropertyGateway.properties(id));
    }

    /** 幂等调整库存，先占用台账请求编号，再更新库存并回填结果。 */
    @Transactional(rollbackFor = Exception.class)
    public StockAdjustment adjustStock(StockAdjustment value) {
        var previous = stockAdjustmentGateway.find(value.requestNo());
        if (previous != null) {
            if (!previous.productId().equals(value.productId()) || previous.delta() != value.delta() || !previous.reason().equals(value.reason())) {
                throw new IllegalStateException("库存请求编号已用于不同的操作");
            }
            return previous;
        }
        stockAdjustmentGateway.insert(value);
        if (!productGateway.adjustQuantity(value)) {
            throw new IllegalStateException("商品不存在或库存调整越界");
        }
        var saved = new StockAdjustment(value.requestNo(),
                value.productId(),
                value.delta(),
                value.reason(),
                productGateway.catalogSku(value.productId()).stock());
        if (!stockAdjustmentGateway.finish(saved)) {
            throw new IllegalStateException("库存台账保存失败");
        }
        return saved;
    }

    /** 小程序只展示当前可见、价格有效的商品与 SKU。 */
    @Transactional(readOnly = true)
    public CatalogSpu product(Long id) {
        if (!spuGateway.visible(id)) {
            return null;
        }
        var value = spu(id);
        return value == null ? null : withSkus(value,
                value.skus().stream().filter(sku -> "ON_SHELF".equals(sku.status()) && sku.price() != null && sku.price().signum() > 0).toList());
    }

    private CatalogSpu withSkus(CatalogSpu value, List<CatalogSku> skus) {
        return new CatalogSpu(value.id(),
                value.code(),
                value.name(),
                value.categoryId(),
                value.brandId(),
                value.description(),
                value.image(),
                value.status(),
                skus);
    }

    private CatalogSku withProperties(CatalogSku value, Map<String, String> properties) {
        return new CatalogSku(value.id(),
                value.spuId(),
                value.code(),
                value.name(),
                value.price(),
                value.stock(),
                value.image(),
                properties,
                value.status());
    }
}
