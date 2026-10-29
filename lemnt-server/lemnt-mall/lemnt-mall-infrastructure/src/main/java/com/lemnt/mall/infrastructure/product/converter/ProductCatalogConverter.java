package com.lemnt.mall.infrastructure.product.converter;

import com.lemnt.mall.domain.product.model.Brand;
import com.lemnt.mall.domain.product.model.Category;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.infrastructure.product.dataobject.BrandDO;
import com.lemnt.mall.infrastructure.product.dataobject.CategoryDO;
import com.lemnt.mall.infrastructure.product.dataobject.ProductSkuDO;
import com.lemnt.mall.infrastructure.product.dataobject.ProductSpuDO;

import com.alibaba.fastjson2.JSON;

import java.util.List;

/**
 * 目录转换器。
 */
public final class ProductCatalogConverter {
    private ProductCatalogConverter() {}

    /**
     * 分类。
     */
    public static Category category(CategoryDO row) {
        return row == null
        ? null
        : new Category(
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
    public static Brand brand(BrandDO row) {
        return row == null
        ? null
        : new Brand(
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
                skus, JSON.parseArray(row.getDeliveryTypes(), Integer.class), row.getDeliveryTemplateId());
    }

    /**
     * 商品SKU。
     */
    public static ProductSku sku(ProductSkuDO row) {
        if (row == null
        ) {
            return null;
        }
        var values = properties(row.getProperties());
        return new ProductSku(
                row.getId(),
                row.getSpuId(),
                row.getProductCode(),
                row.getName(),
                row.getPrice(),
                row.getStock(),
                row.getMainImage(),
                values == null ? null : values.stream().map(property ->
                        new com.lemnt.mall.domain.product.model.ProductSkuSpec(property.propertyId(), property.propertyName(), property.valueId(), property.valueName())).toList(),
                row.getStatus(), row.getWeight(), row.getVolume());
    }

    /**
     * 分类。
     */
    public static CategoryDO category(Category value) {
        var row = new CategoryDO();
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
    public static BrandDO brand(Brand value) {
        var row = new BrandDO();
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
        row.setProperties(JSON.toJSONString(value.properties()));
        row.setMainImage(value.image());
        row.setStatus(value.status());
        row.setVariantKey(ProductCatalogValidation.variantKey(value.properties()));
        row.setWeight(value.weight());
        row.setVolume(value.volume());
        return row;
    }

    /** SKU规格仅从其JSON读取，不补字典现值或猜测旧编号。 */
    public static List<com.lemnt.mall.domain.product.model.ProductSkuSpec> properties(String json) {
        return json == null ? List.of() : JSON.parseArray(json, com.lemnt.mall.domain.product.model.ProductSkuSpec.class);
    }
}
