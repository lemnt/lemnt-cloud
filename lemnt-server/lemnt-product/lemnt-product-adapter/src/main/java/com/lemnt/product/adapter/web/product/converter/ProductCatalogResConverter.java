package com.lemnt.product.adapter.web.product.converter;

import com.lemnt.product.adapter.web.product.res.MallBrandCreateRes;
import com.lemnt.product.adapter.web.product.res.MallBrandDetailRes;
import com.lemnt.product.adapter.web.product.res.MallBrandListRes;
import com.lemnt.product.adapter.web.product.res.MallBrandUpdateRes;
import com.lemnt.product.adapter.web.product.res.MallCategoryCreateRes;
import com.lemnt.product.adapter.web.product.res.MallCategoryDetailRes;
import com.lemnt.product.adapter.web.product.res.MallCategoryListRes;
import com.lemnt.product.adapter.web.product.res.MallCategoryUpdateRes;
import com.lemnt.product.adapter.web.product.res.ProductSkuRes;
import com.lemnt.product.adapter.web.product.res.ProductSpuCreateRes;
import com.lemnt.product.adapter.web.product.res.ProductSpuDetailRes;
import com.lemnt.product.adapter.web.product.res.ProductSpuPageRes;
import com.lemnt.product.adapter.web.product.res.ProductStockAdjustmentRes;
import com.lemnt.product.application.product.dto.ProductBrandDTO;
import com.lemnt.product.application.product.dto.ProductCategoryDTO;
import com.lemnt.product.application.product.dto.ProductSkuDTO;
import com.lemnt.product.application.product.dto.ProductSpuDTO;
import com.lemnt.product.application.product.dto.ProductStockAdjustmentDTO;

/**
 * 目录响应转换器。
 */
public final class ProductCatalogResConverter {
    private ProductCatalogResConverter() {
    }

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
            value.specs() == null
                ? null
                : value.specs().stream()
                .map(
                    property ->
                        new ProductSkuRes.Spec(
                            property.specId(),
                            property.specName(),
                            property.specValueId(),
                            property.specValueName()))
                .toList(),
            value.status(),
            value.weight(),
            value.volume());
    }

    /**
     * 库存调整。
     */
    public static ProductStockAdjustmentRes stockAdjustment(ProductStockAdjustmentDTO value) {
        return value == null
            ? null
            : new ProductStockAdjustmentRes(
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
            value.skus().stream().map(ProductCatalogResConverter::sku).toList(),
            value.keyword(),
            value.introduction(),
            value.sliderPicUrls(),
            value.sort());
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
            value.skus().stream().map(ProductCatalogResConverter::sku).toList(),
            value.deliveryTypes(),
            value.deliveryTemplateId(),
            value.keyword(),
            value.introduction(),
            value.sliderPicUrls(),
            value.sort());
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
            value.skus().stream().map(ProductCatalogResConverter::sku).toList(),
            value.deliveryTypes(),
            value.deliveryTemplateId(),
            value.keyword(),
            value.introduction(),
            value.sliderPicUrls(),
            value.sort());
    }

    /**
     * 分类。
     */
    public static MallCategoryListRes categoryToMallCategoryListRes(ProductCategoryDTO value) {
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
    public static MallCategoryDetailRes categoryToMallCategoryDetailRes(ProductCategoryDTO value) {
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
    public static MallCategoryCreateRes categoryToMallCategoryCreateRes(ProductCategoryDTO value) {
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
    public static MallCategoryUpdateRes categoryToMallCategoryUpdateRes(ProductCategoryDTO value) {
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
    public static MallBrandListRes brandToMallBrandListRes(ProductBrandDTO value) {
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
    public static MallBrandDetailRes brandToMallBrandDetailRes(ProductBrandDTO value) {
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
    public static MallBrandCreateRes brandToMallBrandCreateRes(ProductBrandDTO value) {
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
    public static MallBrandUpdateRes brandToMallBrandUpdateRes(ProductBrandDTO value) {
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
