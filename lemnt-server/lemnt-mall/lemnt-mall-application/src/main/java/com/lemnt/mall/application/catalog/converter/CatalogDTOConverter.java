package com.lemnt.mall.application.catalog.converter;

import com.lemnt.mall.application.catalog.dto.BrandDTO;
import com.lemnt.mall.application.catalog.dto.CategoryDTO;
import com.lemnt.mall.application.catalog.dto.SkuDTO;
import com.lemnt.mall.application.catalog.dto.SpuDTO;
import com.lemnt.mall.application.catalog.dto.StockAdjustmentDTO;
import com.lemnt.mall.domain.catalog.model.Brand;
import com.lemnt.mall.domain.catalog.model.CatalogSku;
import com.lemnt.mall.domain.catalog.model.CatalogSpu;
import com.lemnt.mall.domain.catalog.model.Category;
import com.lemnt.mall.domain.catalog.model.StockAdjustment;

/**
 * 目录数据传输对象转换器。
 */
public final class CatalogDTOConverter {
    private CatalogDTOConverter() {}

    /**
     * 分类。
     */
    public static CategoryDTO category(Category value) {
        return value == null
        ? null
        : new CategoryDTO(
                value.id(),
                value.parentId(),
                value.name(),
                value.picUrl(),
                value.sort(),
                value.status());
    }

    /**
     * 品牌。
     */
    public static BrandDTO brand(Brand value) {
        return value == null
        ? null
        : new BrandDTO(
                value.id(),
                value.name(),
                value.picUrl(),
                value.description(),
                value.sort(),
                value.status());
    }

    /**
     * 商品SKU。
     */
    public static SkuDTO sku(CatalogSku value) {
        return value == null
        ? null
        : new SkuDTO(
                value.id(),
                value.spuId(),
                value.code(),
                value.name(),
                value.price(),
                value.stock(),
                value.image(),
                value.properties(),
                value.status());
    }

    /**
     * 商品SPU。
     */
    public static SpuDTO spu(CatalogSpu value) {
        return value == null
        ? null
        : new SpuDTO(
                value.id(),
                value.code(),
                value.name(),
                value.categoryId(),
                value.brandId(),
                value.description(),
                value.image(),
                value.status(),
                value.skus().stream().map(CatalogDTOConverter::sku).toList());
    }

    /**
     * 调整。
     */
    public static StockAdjustmentDTO adjustment(StockAdjustment value) {
        return new StockAdjustmentDTO(
                value.requestNo(),
                value.productId(),
                value.delta(),
                value.reason(),
                value.resultingStock());
    }
}
