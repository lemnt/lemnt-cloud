package com.lemnt.mall.adapter.web.catalog.converter;

import com.lemnt.mall.application.catalog.dto.*;
import com.lemnt.mall.adapter.web.catalog.res.*;

public final class CatalogResConverter {
    private CatalogResConverter() {}

    public static CategoryRes category(CategoryDTO value) {
        return value == null
                ? null
                : new CategoryRes(
                        value.id(),
                        value.parentId(),
                        value.name(),
                        value.picUrl(),
                        value.sort(),
                        value.status());
    }

    public static BrandRes brand(BrandDTO value) {
        return value == null
                ? null
                : new BrandRes(
                        value.id(),
                        value.name(),
                        value.picUrl(),
                        value.description(),
                        value.sort(),
                        value.status());
    }

    public static SkuRes sku(SkuDTO value) {
        return value == null
                ? null
                : new SkuRes(
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

    public static SpuRes spu(SpuDTO value) {
        return value == null
                ? null
                : new SpuRes(
                        value.id(),
                        value.code(),
                        value.name(),
                        value.categoryId(),
                        value.brandId(),
                        value.description(),
                        value.image(),
                        value.status(),
                        value.skus().stream().map(CatalogResConverter::sku).toList());
    }

    public static StockAdjustmentRes stockAdjustment(StockAdjustmentDTO value) {
        return value == null
                ? null
                : new StockAdjustmentRes(
                        value.requestNo(),
                        value.productId(),
                        value.delta(),
                        value.reason(),
                        value.resultingStock());
    }
}
