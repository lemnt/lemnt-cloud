package com.lemnt.product.infrastructure.product.converter;

import com.lemnt.product.domain.product.model.ProductBrand;
import com.lemnt.product.domain.product.model.ProductCategory;
import com.lemnt.product.domain.product.model.ProductCatalogValidation;
import com.lemnt.product.domain.product.model.ProductSku;
import com.lemnt.product.domain.product.model.ProductSkuSpec;
import com.lemnt.product.domain.product.model.ProductSpu;
import com.lemnt.product.infrastructure.product.dataobject.ProductBrandDO;
import com.lemnt.product.infrastructure.product.dataobject.ProductCategoryDO;
import com.lemnt.product.infrastructure.product.dataobject.ProductSkuDO;
import com.lemnt.product.infrastructure.product.dataobject.ProductSpuDO;

import com.alibaba.fastjson2.JSON;

import java.util.List;

/**
 * 目录转换器。
 */
public final class ProductCatalogConverter {
    private ProductCatalogConverter() {
    }

    /**
     * 分类。
     */
    public static ProductCategory category(ProductCategoryDO row) {
        return row == null
            ? null
            : new ProductCategory(
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
    public static ProductBrand brand(ProductBrandDO row) {
        return row == null
            ? null
            : new ProductBrand(
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
    public static ProductSpu spu(ProductSpuDO row, List<ProductSku> skus) {
        return row == null
            ? null
            : new ProductSpu(
            row.getId(),
            row.getCode(),
            row.getName(),
            row.getCategoryId(),
            row.getBrandId(),
            row.getDescription(),
            row.getImage(),
            row.getStatus(),
            skus,
            JSON.parseArray(row.getDeliveryTypes(), Integer.class),
            row.getDeliveryTemplateId(),
            row.getKeyword(),
            row.getIntroduction(),
            JSON.parseArray(row.getSliderPicUrls(), String.class),
            row.getSort());
    }

    /**
     * 商品SKU。
     */
    public static ProductSku sku(ProductSkuDO row) {
        if (row == null) {
            return null;
        }
        var values = specs(row.getSpecs());
        return new ProductSku(
            row.getId(),
            row.getSpuId(),
            row.getProductCode(),
            row.getName(),
            row.getPrice(),
            row.getStock(),
            row.getMainImage(),
            values == null
                ? null
                : values.stream()
                .map(
                    property ->
                        new ProductSkuSpec(
                            property.specId(),
                            property.specName(),
                            property.specValueId(),
                            property.specValueName()))
                .toList(),
            row.getStatus(),
            row.getWeight(),
            row.getVolume());
    }

    /**
     * 分类。
     */
    public static ProductCategoryDO category(ProductCategory value) {
        var row = new ProductCategoryDO();
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
    public static ProductBrandDO brand(ProductBrand value) {
        var row = new ProductBrandDO();
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
    public static ProductSpuDO spu(ProductSpu value) {
        var row = new ProductSpuDO();
        row.setId(value.id());
        row.setCode(value.code());
        row.setName(value.name());
        row.setCategoryId(value.categoryId());
        row.setBrandId(value.brandId());
        row.setDescription(value.description());
        row.setImage(value.image());
        row.setStatus(value.status());
        row.setDeliveryTypes(JSON.toJSONString(value.deliveryTypes()));
        row.setDeliveryTemplateId(value.deliveryTemplateId());
        row.setKeyword(value.keyword());
        row.setIntroduction(value.introduction());
        row.setSliderPicUrls(
            value.sliderPicUrls() == null ? null : JSON.toJSONString(value.sliderPicUrls()));
        row.setSort(value.sort());
        return row;
    }

    /**
     * 商品SKU。
     */
    public static ProductSkuDO sku(ProductSku value) {
        var row = new ProductSkuDO();
        row.setId(value.id());
        row.setSpuId(value.spuId());
        row.setProductCode(value.code());
        row.setName(value.name());
        row.setPrice(value.price());
        row.setStock(value.stock());
        row.setSpecs(JSON.toJSONString(value.specs()));
        row.setMainImage(value.image());
        row.setStatus(value.status());
        row.setVariantKey(ProductCatalogValidation.variantKey(value.specs()));
        row.setWeight(value.weight());
        row.setVolume(value.volume());
        return row;
    }

    /**
     * SKU规格仅从其JSON读取，不补字典现值或猜测旧编号。
     */
    public static List<ProductSkuSpec> specs(String json) {
        return json == null ? List.of() : ProductSpecSnapshotJson.read(json, ProductSkuSpec.class);
    }
}
