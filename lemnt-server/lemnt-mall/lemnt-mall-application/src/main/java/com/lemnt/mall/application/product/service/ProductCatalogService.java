package com.lemnt.mall.application.product.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
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
import java.util.Objects;
import java.util.TreeSet;

/** ProductCatalogService 跨表应用编排，保持原有事务和锁顺序。 */
@Component
@RequiredArgsConstructor
public class ProductCatalogService {
    private final ProductSpuGateway productSpuGateway;
    private final ProductSkuGateway productSkuGateway;
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
            productSkuGateway.insertSku(new ProductSku(null,
                    saved.id(),
                    sku.code(),
                    sku.name(),
                    sku.price(),
                    sku.stock(),
                    sku.image(),
                sku.properties(),
                    sku.status(), sku.weight(), sku.volume()));
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
        // 内部历史调用未携带经营字段时保留原配置；HTTP编辑请求要求显式填写。
        value = new ProductSpu(value.id(), value.code(), value.name(), value.categoryId(), value.brandId(),
                value.description(), value.image(), value.status(), value.skus(), value.deliveryTypes(), value.deliveryTemplateId(),
                value.keyword() == null ? original.keyword() : value.keyword(),
                value.introduction() == null ? original.introduction() : value.introduction(),
                value.sliderPicUrls() == null ? original.sliderPicUrls() : value.sliderPicUrls(),
                value.sort() == null ? original.sort() : value.sort());
        var validated = ProductCatalogValidation.newSpu(
                new ProductSpu(value.id(), original.code(), value.name(), value.categoryId(), value.brandId(),
                        value.description(), value.image(), original.status(), value.skus(), value.deliveryTypes(), value.deliveryTemplateId(), value.keyword(), value.introduction(), value.sliderPicUrls(), value.sort()));
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
                        sku.name(), sku.price(), sku.stock(), sku.image(),
                sku.properties(), "ON_SHELF", sku.weight(), sku.volume()));
            } else {
                var old = existing.stream().filter(item -> item.id().equals(sku.id())).findFirst()
                        .orElseThrow(() -> new IllegalArgumentException("SKU 不属于当前商品"));
                if (!retained.add(sku.id()) || !old.code().equals(sku.code())
                ) {
                    throw new IllegalArgumentException("SKU 编号重复或编码已被修改");
                }
                productSkuGateway.lockById(sku.id());
                if (!productSkuGateway.updateSku(new ProductSku(sku.id(), value.id(), sku.code(), sku.name(),
                        sku.price(), sku.stock(), sku.image(),
                sku.properties(), "ON_SHELF", sku.weight(), sku.volume()))
                ) {
                    throw new IllegalStateException("SKU 保存失败");
                }
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
        return withSkus(value, productSkuGateway.catalogSkus(id));
    }

    /** 组合 SKU 主体和属性。 */
    @Transactional(readOnly = true)
    public ProductSku sku(Long id) {
        return productSkuGateway.catalogSku(id);
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
                skus, value.deliveryTypes(), value.deliveryTemplateId(), value.keyword(), value.introduction(), value.sliderPicUrls(), value.sort());
    }

    private ProductSku withProperties(ProductSku value, List<ProductSkuSpec> properties) {
        return new ProductSku(value.id(),
                value.spuId(),
                value.code(),
                value.name(),
                value.price(),
                value.stock(),
                value.image(),
                properties == null ? null : properties.stream().map(property ->
                        new com.lemnt.mall.domain.product.model.ProductSkuSpec(property.propertyId(), property.propertyName(), property.valueId(), property.valueName())).toList(),
                value.status(), value.weight(), value.volume());
    }

    /** 按编号锁字典，直接生成含编号及名称的规格列表。 */
    private ProductSpu resolveProperties(ProductSpu value) {
        if (value == null || value.skus().isEmpty() || value.skus().size() > 100
                || value.skus().stream().anyMatch(Objects::isNull)) {
            throw new ServiceException("商品SKU不合法", 400);
        }
        var all = value.skus().stream().flatMap(sku -> sku.properties().stream()).toList();
        if (value.skus().stream().anyMatch(sku -> sku.properties().size() > 10)
                || all.stream().anyMatch(property -> (property.propertyId() == null) != (property.valueId() == null)
                        || (property.propertyId() != null && (property.propertyId() <= 0 || property.valueId() <= 0)))) {
            throw new ServiceException("商品规格编号不合法", 400);
        }
        boolean linked = all.stream().anyMatch(property -> property.propertyId() != null);
        if (!linked) {
            return value; // 存量名值资料只用于内部读取/兼容入口，HTTP请求必须提供编号。
        }
        if (all.stream().anyMatch(property -> property.propertyId() == null)) {
            throw new ServiceException("同一商品SKU必须选取相同规格项", 400);
        }
        var expected = value.skus().get(0).properties().stream().map(ProductSkuSpec::propertyId)
                .collect(java.util.stream.Collectors.toSet());
        for (var sku : value.skus()) {
            var selected = sku.properties().stream().map(ProductSkuSpec::propertyId)
                    .collect(java.util.stream.Collectors.toSet());
            if (selected.size() != sku.properties().size() || !selected.equals(expected)) {
                throw new ServiceException("SKU规格重复或规格项集合不一致", 400);
            }
        }
        var specs = new HashMap<Long, ProductSpec>();
        new TreeSet<>(all.stream().map(ProductSkuSpec::propertyId).toList()).forEach(id -> {
            var spec = productSpecGateway.lock(id);
            if (spec == null) { throw new ServiceException("商品规格不存在", 400); }
            specs.put(id, spec);
        });
        var options = new HashMap<Long, ProductSpecValue>();
        new TreeSet<>(all.stream().map(ProductSkuSpec::valueId).toList()).forEach(id -> {
            var option = productSpecValueGateway.lock(id);
            if (option == null) { throw new ServiceException("商品规格值不存在", 400); }
            options.put(id, option);
        });
        var skus = value.skus().stream().map(sku -> withProperties(sku, sku.properties().stream().map(property -> {
            var spec = specs.get(property.propertyId());
            var option = options.get(property.valueId());
            if (!spec.id().equals(option.specId())) {
                throw new ServiceException("规格值不属于所选规格项", 400);
            }
            if ((property.propertyName() != null && !spec.name().equals(property.propertyName()))
                    || (property.valueName() != null && !option.name().equals(property.valueName()))) {
                throw new ServiceException("规格名称和值必须与字典一致", 400);
            }
            return new ProductSkuSpec(spec.id(), spec.name(), option.id(), option.name());
        }).toList())).toList();
        return withSkus(value, skus);
    }

}
