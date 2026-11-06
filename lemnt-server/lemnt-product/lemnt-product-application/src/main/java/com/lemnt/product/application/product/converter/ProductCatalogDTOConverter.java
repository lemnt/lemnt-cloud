package com.lemnt.product.application.product.converter;

import com.lemnt.product.application.product.dto.ProductBrandDTO;
import com.lemnt.product.application.product.dto.ProductCategoryDTO;
import com.lemnt.product.application.product.dto.ProductSkuDTO;
import com.lemnt.product.application.product.dto.ProductSpuDTO;
import com.lemnt.product.domain.product.model.ProductBrand;
import com.lemnt.product.domain.product.model.ProductCategory;
import com.lemnt.product.domain.product.model.ProductSku;
import com.lemnt.product.domain.product.model.ProductSpu;

/**
 * 目录数据传输对象转换器。
 */
public final class ProductCatalogDTOConverter {
    private ProductCatalogDTOConverter() {
    }

    /**
     * 分类。
     */
    public static ProductCategoryDTO category(ProductCategory value) {
        return value == null
            ? null
            : new ProductCategoryDTO(
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
    public static ProductBrandDTO brand(ProductBrand value) {
        return value == null
            ? null
            : new ProductBrandDTO(
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
            value.specs() == null
                ? null
                : value.specs().stream()
                .map(
                    property ->
                        new ProductSkuDTO.Spec(
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
            value.skus().stream().map(ProductCatalogDTOConverter::sku).toList(),
            value.deliveryTypes(),
            value.deliveryTemplateId(),
            value.keyword(),
            value.introduction(),
            value.sliderPicUrls(),
            value.sort());
    }
}
