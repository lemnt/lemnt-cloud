package com.lemnt.mall.application.product.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSkuSpecGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpecGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpecValueGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.gateway.StockReservationGateway;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSkuSpec;
import com.lemnt.mall.domain.product.model.ProductSpec;
import com.lemnt.mall.domain.product.model.ProductSpecValue;
import com.lemnt.mall.domain.product.model.ProductSpu;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** ProductCatalogService 跨表应用编排，保持原有事务和锁顺序。 */
@Component
@RequiredArgsConstructor
public class ProductCatalogService {
    private final ProductSpuGateway productSpuGateway;
    private final ProductSkuGateway productSkuGateway;
    private final ProductSkuSpecGateway productSkuSpecGateway;
    private final StockReservationGateway stockReservationGateway;
    private final ProductSpecGateway productSpecGateway;
    private final ProductSpecValueGateway productSpecValueGateway;

    /** 创建商品主体、SKU 和属性，任一步失败整体回滚。 */
    @Transactional(rollbackFor = Exception.class)
    public ProductSpu createSpu(ProductSpu input) {
        var value = ProductCatalogValidation.newSpu(resolveProperties(input));
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
                    sku.status(), sku.weight(), sku.volume(), sku.specValueIds()));
            saveProperties(child.id(), sku);
        }
        return spu(saved.id());
    }

    /** 按 SPU 原子保存完整 SKU 集合，价格、库存和规格不再通过独立后台接口修改。 */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSpu(ProductSpu input) {
        var value = resolveProperties(input);
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
                        sku.name(), sku.price(), sku.stock(), sku.image(), sku.properties(), "ON_SHELF", sku.weight(), sku.volume(), sku.specValueIds()));
                saveProperties(saved.id(), sku);
            } else {
                var old = existing.stream().filter(item -> item.id().equals(sku.id())).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("SKU 不属于当前商品"));
                if (!retained.add(sku.id()) || !old.code().equals(sku.code())
                ) {
                    throw new IllegalArgumentException("SKU 编号重复或编码已被修改");
                }
                productSkuGateway.lockById(sku.id());
                if (!productSkuGateway.updateSku(new ProductSku(sku.id(), value.id(), sku.code(), sku.name(),
                        sku.price(), sku.stock(), sku.image(), sku.properties(), "ON_SHELF", sku.weight(), sku.volume(), sku.specValueIds()))
                ) {
                    throw new IllegalStateException("SKU 保存失败");
                }
                saveProperties(sku.id(), sku);
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
        var specs = productSkuSpecGateway.specsBySpu(id);
        var skus = productSkuGateway.catalogSkus(id).stream().map(sku -> {
            var rows = specs.getOrDefault(sku.id(), List.of());
            return withProperties(sku, ProductSkuSpec.names(rows), ProductSkuSpec.identities(rows));
        }).toList();
        return withSkus(value, skus);
    }

    /** 组合 SKU 主体和属性。 */
    @Transactional(readOnly = true)
    public ProductSku sku(Long id) {
        var value = productSkuGateway.catalogSku(id);
        if (value == null) {
            return null;
        }
        var rows = productSkuSpecGateway.specsBySkuIds(List.of(id)).getOrDefault(id, List.of());
        return withProperties(value, ProductSkuSpec.names(rows), ProductSkuSpec.identities(rows));
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

    private ProductSku withProperties(ProductSku value, Map<String, String> properties, Map<Long, Long> ids) {
        return new ProductSku(value.id(),
                value.spuId(),
                value.code(),
                value.name(),
                value.price(),
                value.stock(),
                value.image(),
                properties,
                value.status(), value.weight(), value.volume(), ids);
    }

    /** 先按编号锁字典再解析名称，避免规格删除与商品写入交错。 */
    private ProductSpu resolveProperties(ProductSpu value) {
        if (value == null || value.skus().isEmpty() || value.skus().size() > 100
                || value.skus().stream().anyMatch(Objects::isNull)) {
            throw new ServiceException("商品SKU不合法", 400);
        }
        var ids = new TreeSet<Long>();
        for (var sku : value.skus()) {
            if (sku.specValueIds() != null) {
                if (sku.specValueIds().size() > 10 || sku.specValueIds().entrySet().stream().anyMatch(entry ->
                        entry.getKey() <= 0 || entry.getValue() <= 0)) {
                    throw new ServiceException("商品规格编号不合法", 400);
                }
                ids.addAll(sku.specValueIds().keySet());
            }
        }
        var specs = new HashMap<Long, ProductSpec>();
        ids.forEach(id -> {
            var spec = productSpecGateway.lock(id);
            if (spec == null) {
                throw new ServiceException("商品规格不存在", 400);
            }
            specs.put(id, spec);
        });
        var valueIds = new TreeSet<Long>();
        value.skus().stream().filter(sku -> sku.specValueIds() != null)
                .forEach(sku -> valueIds.addAll(sku.specValueIds().values()));
        var options = new HashMap<Long, ProductSpecValue>();
        valueIds.forEach(id -> {
            var option = productSpecValueGateway.lock(id);
            if (option == null) {
                throw new ServiceException("商品规格值不存在", 400);
            }
            options.put(id, option);
        });
        boolean linked = value.skus().stream().anyMatch(sku -> sku.specValueIds() != null);
        Set<Long> expected = linked ? value.skus().stream().filter(sku -> sku.specValueIds() != null)
                .findFirst().orElseThrow().specValueIds().keySet() : null;
        var skus = value.skus().stream().map(sku -> {
            if (linked && (sku.specValueIds() == null || !sku.specValueIds().keySet().equals(expected))) {
                throw new ServiceException("同一商品SKU必须选取相同规格项", 400);
            }
            if (sku.specValueIds() == null) {
                return sku;
            }
            var names = new TreeMap<String, String>();
            sku.specValueIds().forEach((id, optionId) -> {
                var option = options.get(optionId);
                if (!id.equals(option.specId())) {
                    throw new ServiceException("规格值不属于所选规格项", 400);
                }
                names.put(specs.get(id).name(), option.name());
            });
            if (!sku.properties().isEmpty() && !sku.properties().equals(names)) {
                throw new ServiceException("规格名称和值必须与字典一致", 400);
            }
            return withProperties(sku, names, sku.specValueIds());
        }).toList();
        return withSkus(value, skus);
    }

    /** 字典锁持有至事务结束，保存编号和对应名称，禁止依赖Map迭代顺序配对。 */
    private void saveProperties(Long skuId, ProductSku sku) {
        if (sku.specValueIds() == null) {
            productSkuSpecGateway.saveProperties(skuId, sku.properties());
            return;
        }
        var values = sku.specValueIds().entrySet().stream().map(entry -> {
            var spec = productSpecGateway.find(entry.getKey());
            return new ProductSkuSpec(entry.getKey(), spec.name(),
                    entry.getValue(), sku.properties().get(spec.name()));
        }).toList();
        productSkuSpecGateway.saveSpecs(skuId, values);
    }
}
