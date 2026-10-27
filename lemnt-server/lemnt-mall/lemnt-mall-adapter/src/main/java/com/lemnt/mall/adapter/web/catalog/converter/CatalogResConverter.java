package com.lemnt.mall.adapter.web.catalog.converter;

import com.lemnt.mall.adapter.web.catalog.res.MallBrandCreateRes;
import com.lemnt.mall.adapter.web.catalog.res.MallBrandDetailRes;
import com.lemnt.mall.adapter.web.catalog.res.MallBrandListRes;
import com.lemnt.mall.adapter.web.catalog.res.MallBrandUpdateRes;
import com.lemnt.mall.adapter.web.catalog.res.MallCategoryCreateRes;
import com.lemnt.mall.adapter.web.catalog.res.MallCategoryDetailRes;
import com.lemnt.mall.adapter.web.catalog.res.MallCategoryListRes;
import com.lemnt.mall.adapter.web.catalog.res.MallCategoryUpdateRes;
import com.lemnt.mall.adapter.web.catalog.res.MallSpuCreateRes;
import com.lemnt.mall.adapter.web.catalog.res.MallSpuDetailRes;
import com.lemnt.mall.adapter.web.catalog.res.MallSpuPageRes;
import com.lemnt.mall.adapter.web.catalog.res.SkuRes;
import com.lemnt.mall.adapter.web.catalog.res.StockAdjustmentRes;
import com.lemnt.mall.application.catalog.dto.BrandDTO;
import com.lemnt.mall.application.catalog.dto.CategoryDTO;
import com.lemnt.mall.application.catalog.dto.SkuDTO;
import com.lemnt.mall.application.catalog.dto.SpuDTO;
import com.lemnt.mall.application.catalog.dto.StockAdjustmentDTO;

/**
 * 目录响应转换器。
 */
public final class CatalogResConverter {
    private CatalogResConverter() {}

    /**
     * 商品SKU。
     */
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

    /**
     * 库存调整。
     */
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

    /**
     * 商品SPU。
     */
    public static MallSpuPageRes spuToMallSpuPageRes(SpuDTO value) {
        return value == null
                ? null
                : new MallSpuPageRes(
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

    /**
     * 商品SPU。
     */
    public static MallSpuDetailRes spuToMallSpuDetailRes(SpuDTO value) {
        return value == null
                ? null
                : new MallSpuDetailRes(
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

    /**
     * 商品SPU。
     */
    public static MallSpuCreateRes spuToMallSpuCreateRes(SpuDTO value) {
        return value == null
                ? null
                : new MallSpuCreateRes(
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

    /**
     * 分类。
     */
    public static MallCategoryListRes categoryToMallCategoryListRes(CategoryDTO value) {
        return value == null
                ? null
                : new MallCategoryListRes(
                value.id(),
                value.parentId(),
                value.name(),
                value.picUrl(),
                value.sort(),
                value.status());
    }

    /**
     * 分类。
     */
    public static MallCategoryDetailRes categoryToMallCategoryDetailRes(CategoryDTO value) {
        return value == null
                ? null
                : new MallCategoryDetailRes(
                value.id(),
                value.parentId(),
                value.name(),
                value.picUrl(),
                value.sort(),
                value.status());
    }

    /**
     * 分类。
     */
    public static MallCategoryCreateRes categoryToMallCategoryCreateRes(CategoryDTO value) {
        return value == null
                ? null
                : new MallCategoryCreateRes(
                value.id(),
                value.parentId(),
                value.name(),
                value.picUrl(),
                value.sort(),
                value.status());
    }

    /**
     * 分类。
     */
    public static MallCategoryUpdateRes categoryToMallCategoryUpdateRes(CategoryDTO value) {
        return value == null
                ? null
                : new MallCategoryUpdateRes(
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
    public static MallBrandListRes brandToMallBrandListRes(BrandDTO value) {
        return value == null
                ? null
                : new MallBrandListRes(
                value.id(),
                value.name(),
                value.picUrl(),
                value.description(),
                value.sort(),
                value.status());
    }

    /**
     * 品牌。
     */
    public static MallBrandDetailRes brandToMallBrandDetailRes(BrandDTO value) {
        return value == null
                ? null
                : new MallBrandDetailRes(
                value.id(),
                value.name(),
                value.picUrl(),
                value.description(),
                value.sort(),
                value.status());
    }

    /**
     * 品牌。
     */
    public static MallBrandCreateRes brandToMallBrandCreateRes(BrandDTO value) {
        return value == null
                ? null
                : new MallBrandCreateRes(
                value.id(),
                value.name(),
                value.picUrl(),
                value.description(),
                value.sort(),
                value.status());
    }

    /**
     * 品牌。
     */
    public static MallBrandUpdateRes brandToMallBrandUpdateRes(BrandDTO value) {
        return value == null
                ? null
                : new MallBrandUpdateRes(
                value.id(),
                value.name(),
                value.picUrl(),
                value.description(),
                value.sort(),
                value.status());
    }
}
