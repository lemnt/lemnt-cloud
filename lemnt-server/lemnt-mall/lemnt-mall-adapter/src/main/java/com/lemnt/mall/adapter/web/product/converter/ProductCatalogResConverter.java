package com.lemnt.mall.adapter.web.product.converter;

import com.lemnt.mall.adapter.web.product.res.MallBrandCreateRes;
import com.lemnt.mall.adapter.web.product.res.MallBrandDetailRes;
import com.lemnt.mall.adapter.web.product.res.MallBrandListRes;
import com.lemnt.mall.adapter.web.product.res.MallBrandUpdateRes;
import com.lemnt.mall.adapter.web.product.res.MallCategoryCreateRes;
import com.lemnt.mall.adapter.web.product.res.MallCategoryDetailRes;
import com.lemnt.mall.adapter.web.product.res.MallCategoryListRes;
import com.lemnt.mall.adapter.web.product.res.MallCategoryUpdateRes;
import com.lemnt.mall.adapter.web.product.res.ProductSkuRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpuCreateRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpuDetailRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpuPageRes;
import com.lemnt.mall.adapter.web.product.res.StockAdjustmentRes;
import com.lemnt.mall.application.product.dto.BrandDTO;
import com.lemnt.mall.application.product.dto.CategoryDTO;
import com.lemnt.mall.application.product.dto.ProductSkuDTO;
import com.lemnt.mall.application.product.dto.ProductSpuDTO;
import com.lemnt.mall.application.product.dto.StockAdjustmentDTO;

/**
 * 目录响应转换器。
 */
public final class ProductCatalogResConverter {
    private ProductCatalogResConverter() {}

    /**
     * 商品SKU。
     */
    public static ProductSkuRes sku(ProductSkuDTO value) {
        return value == null
                ? null
                : new ProductSkuRes(
                value.id(),
                value.spuId(),
                value.code(),
                value.name(),
                value.price(),
                value.stock(),
                value.image(),
                value.properties(),
                value.status(), value.weight(), value.volume());
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
    public static ProductSpuPageRes spuToProductSpuPageRes(ProductSpuDTO value) {
        return value == null
                ? null
                : new ProductSpuPageRes(
                value.id(),
                value.code(),
                value.name(),
                value.categoryId(),
                value.brandId(),
                value.description(),
                value.image(),
                value.status(),
                value.skus().stream().map(ProductCatalogResConverter::sku).toList());
    }

    /**
     * 商品SPU。
     */
    public static ProductSpuDetailRes spuToProductSpuDetailRes(ProductSpuDTO value) {
        return value == null
                ? null
                : new ProductSpuDetailRes(
                value.id(),
                value.code(),
                value.name(),
                value.categoryId(),
                value.brandId(),
                value.description(),
                value.image(),
                value.status(),
                value.skus().stream().map(ProductCatalogResConverter::sku).toList(), value.deliveryTypes(), value.deliveryTemplateId());
    }

    /**
     * 商品SPU。
     */
    public static ProductSpuCreateRes spuToProductSpuCreateRes(ProductSpuDTO value) {
        return value == null
                ? null
                : new ProductSpuCreateRes(
                value.id(),
                value.code(),
                value.name(),
                value.categoryId(),
                value.brandId(),
                value.description(),
                value.image(),
                value.status(),
                value.skus().stream().map(ProductCatalogResConverter::sku).toList(), value.deliveryTypes(), value.deliveryTemplateId());
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
