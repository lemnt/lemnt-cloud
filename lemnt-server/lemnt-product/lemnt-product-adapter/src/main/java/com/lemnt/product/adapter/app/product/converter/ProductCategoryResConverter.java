package com.lemnt.product.adapter.app.product.converter;

import com.lemnt.product.adapter.app.product.res.AppProductCategorySimpleRes;
import com.lemnt.product.adapter.app.product.res.StoreDetailRes;
import com.lemnt.product.adapter.app.product.res.StoreProductRes;
import com.lemnt.product.adapter.app.product.res.StoreSkuRes;
import com.lemnt.product.application.product.dto.*;

/**
 * 门店响应转换器。
 */
public final class ProductCategoryResConverter {
    private ProductCategoryResConverter() {
    }

    /**
     * 商品。
     */
    public static StoreProductRes product(StoreProductDTO v) {
        return v == null
            ? null
            : new StoreProductRes(
            v.id(),
            v.name(),
            v.categoryId(),
            v.brandId(),
            v.image(),
            v.minPrice(),
            v.maxPrice(),
            v.available(),
            v.salesCount());
    }

    /**
     * 商品SKU。
     */
    public static StoreSkuRes sku(StoreSkuDTO v) {
        return v == null
            ? null
            : new StoreSkuRes(
            v.id(),
            v.name(),
            v.price(),
            v.stock(),
            v.image(),
            v.specs() == null
                ? null
                : v.specs().stream()
                .map(
                    property ->
                        new StoreSkuRes.Spec(
                            property.specId(),
                            property.specName(),
                            property.specValueId(),
                            property.specValueName()))
                .toList());
    }

    /**
     * 分类。
     */
    public static AppProductCategorySimpleRes category(ProductCategoryDTO v) {
        return v == null ? null : new AppProductCategorySimpleRes(v.id(), v.parentId(), v.name(), v.picUrl());
    }

    /**
     * 详情。
     */
    public static StoreDetailRes detail(StoreDetailDTO v) {
        return v == null
            ? null
            : new StoreDetailRes(
            v.id(),
            v.name(),
            v.categoryId(),
            v.brandId(),
            v.description(),
            v.image(),
            v.skus().stream().map(ProductCategoryResConverter::sku).toList(),
            v.salesCount());
    }
}
