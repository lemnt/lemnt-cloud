package com.lemnt.mall.infrastructure.catalog.converter;

import com.lemnt.mall.domain.catalog.model.Brand;
import com.lemnt.mall.domain.catalog.model.CatalogRules;
import com.lemnt.mall.domain.catalog.model.CatalogSku;
import com.lemnt.mall.domain.catalog.model.CatalogSpu;
import com.lemnt.mall.domain.catalog.model.Category;
import com.lemnt.mall.domain.catalog.model.StockAdjustment;
import com.lemnt.mall.infrastructure.catalog.dataobject.BrandDO;
import com.lemnt.mall.infrastructure.catalog.dataobject.CategoryDO;
import com.lemnt.mall.infrastructure.catalog.dataobject.SkuDO;
import com.lemnt.mall.infrastructure.catalog.dataobject.SkuPropertyDO;
import com.lemnt.mall.infrastructure.catalog.dataobject.SpuDO;
import com.lemnt.mall.infrastructure.catalog.dataobject.StockAdjustmentDO;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 目录转换器。
 */
public final class CatalogConverter {
    private CatalogConverter() {}

    /**
     * 分类。
     */
    public static Category category(CategoryDO row) {
        return row == null
        ? null
        : new Category(
                row.getId(),
                row.getParentId(),
                row.getName(),
                row.getPicUrl(),
                row.getSort(),
                row.getStatus());
    }

    /**
     * 品牌。
     */
    public static Brand brand(BrandDO row) {
        return row == null
        ? null
        : new Brand(
                row.getId(),
                row.getName(),
                row.getPicUrl(),
                row.getDescription(),
                row.getSort(),
                row.getStatus());
    }

    /**
     * 商品SPU。
     */
    public static CatalogSpu spu(SpuDO row, List<CatalogSku> skus) {
        return row == null
        ? null
        : new CatalogSpu(
                row.getId(),
                row.getCode(),
                row.getName(),
                row.getCategoryId(),
                row.getBrandId(),
                row.getDescription(),
                row.getImage(),
                row.getStatus(),
                skus);
    }

    /**
     * 商品SKU。
     */
    public static CatalogSku sku(SkuDO row, List<SkuPropertyDO> properties) {
        if (row == null) {
            return null;
        }
        Map<String, String> values = new TreeMap<>();
        properties.forEach(p -> values.put(p.getPropertyName(), p.getPropertyValue()));
        return new CatalogSku(
                row.getId(),
                row.getSpuId(),
                row.getCode(),
                row.getName(),
                row.getPrice(),
                row.getStock(),
                row.getImage(),
                values,
                row.getStatus());
    }

    /**
     * 调整。
     */
    public static StockAdjustment adjustment(StockAdjustmentDO row) {
        return row == null
        ? null
        : new StockAdjustment(
                row.getRequestNo(),
                row.getProductId(),
                row.getDelta(),
                row.getReason(),
                row.getResultingStock());
    }

    /**
     * 分类。
     */
    public static CategoryDO category(Category value) {
        var row = new CategoryDO();
        row.setId(value.id());
        row.setParentId(value.parentId());
        row.setName(value.name());
        row.setPicUrl(value.picUrl());
        row.setSort(value.sort());
        row.setStatus(value.status());
        return row;
    }

    /**
     * 品牌。
     */
    public static BrandDO brand(Brand value) {
        var row = new BrandDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setPicUrl(value.picUrl());
        row.setDescription(value.description());
        row.setSort(value.sort());
        row.setStatus(value.status());
        return row;
    }

    /**
     * 商品SPU。
     */
    public static SpuDO spu(CatalogSpu value) {
        var row = new SpuDO();
        row.setId(value.id());
        row.setCode(value.code());
        row.setName(value.name());
        row.setCategoryId(value.categoryId());
        row.setBrandId(value.brandId());
        row.setDescription(value.description());
        row.setImage(value.image());
        row.setStatus(value.status());
        return row;
    }

    /**
     * 商品SKU。
     */
    public static SkuDO sku(CatalogSku value) {
        var row = new SkuDO();
        row.setId(value.id());
        row.setSpuId(value.spuId());
        row.setCode(value.code());
        row.setName(value.name());
        row.setPrice(value.price());
        row.setStock(value.stock());
        row.setImage(value.image());
        row.setStatus(value.status());
        row.setVariantKey(CatalogRules.variantKey(value.properties()));
        return row;
    }
}
