package com.lemnt.mall.application.catalog.converter;

import com.lemnt.mall.application.catalog.dto.*;
import com.lemnt.mall.domain.catalog.model.*;

public final class CatalogDTOConverter {
    private CatalogDTOConverter() {}

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

    public static StockAdjustmentDTO adjustment(StockAdjustment value) {
        return new StockAdjustmentDTO(
                value.requestNo(),
                value.productId(),
                value.delta(),
                value.reason(),
                value.resultingStock());
    }
}
