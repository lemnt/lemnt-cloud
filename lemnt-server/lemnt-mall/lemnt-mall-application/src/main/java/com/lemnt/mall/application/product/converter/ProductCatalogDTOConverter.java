package com.lemnt.mall.application.product.converter;

import com.lemnt.mall.application.product.dto.BrandDTO;
import com.lemnt.mall.application.product.dto.CategoryDTO;
import com.lemnt.mall.application.product.dto.ProductSkuDTO;
import com.lemnt.mall.application.product.dto.ProductSpuDTO;
import com.lemnt.mall.domain.product.model.Brand;
import com.lemnt.mall.domain.product.model.Category;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSpu;

/**
 * 目录数据传输对象转换器。
 */
public final class ProductCatalogDTOConverter {
    private ProductCatalogDTOConverter() {}

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
    public static ProductSkuDTO sku(ProductSku value) {
        return value == null
        ? null
        : new ProductSkuDTO(
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
     * 商品SPU。
     */
    public static ProductSpuDTO spu(ProductSpu value) {
        return value == null
        ? null
        : new ProductSpuDTO(
                value.id(),
                value.code(),
                value.name(),
                value.categoryId(),
                value.brandId(),
                value.description(),
                value.image(),
                value.status(),
                value.skus().stream().map(ProductCatalogDTOConverter::sku).toList(), value.deliveryTypes(), value.deliveryTemplateId());
    }

}
