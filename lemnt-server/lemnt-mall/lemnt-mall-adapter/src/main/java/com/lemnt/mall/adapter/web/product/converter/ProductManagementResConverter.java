package com.lemnt.mall.adapter.web.product.converter;

import com.lemnt.mall.adapter.web.product.res.BrandPageRes;
import com.lemnt.mall.adapter.web.product.res.BrandSimpleRes;
import com.lemnt.mall.adapter.web.product.res.CategoryTreeRes;
import com.lemnt.mall.adapter.web.product.res.ProductSkuAddRes;
import com.lemnt.mall.adapter.web.product.res.ProductSkuDetailRes;
import com.lemnt.mall.adapter.web.product.res.ProductSkuPageRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpuBatchRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpuBatchSkuRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpuCountRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpuSimpleRes;
import com.lemnt.mall.application.product.dto.BrandDTO;
import com.lemnt.mall.application.product.dto.CategoryTreeDTO;
import com.lemnt.mall.application.product.dto.ProductSkuDTO;
import com.lemnt.mall.application.product.dto.ProductSpuDTO;
import com.lemnt.mall.application.product.dto.ProductSpuStatusCountDTO;

/** 商品管理接口独立响应转换。 */
public final class ProductManagementResConverter {
    private ProductManagementResConverter() {
    }

    /** 转换SKU新增响应。 */
    public static ProductSkuAddRes skuAdd(ProductSkuDTO value) {
        return new ProductSkuAddRes(value.id(), value.spuId(), value.code(), value.name(), value.price(), value.stock(),
            value.image(),
                value.properties() == null ? null : value.properties().stream().map(property ->
                        new ProductSkuAddRes.Property(property.propertyId(), property.propertyName(), property.valueId(), property.valueName())).toList(), value.status());
    }

    /** 转换SKU详情响应。 */
    public static ProductSkuDetailRes skuDetail(ProductSkuDTO value) {
        return new ProductSkuDetailRes(value.id(), value.spuId(), value.code(), value.name(), value.price(),
            value.stock(),
            value.image(),
                value.properties() == null ? null : value.properties().stream().map(property ->
                        new ProductSkuDetailRes.Property(property.propertyId(), property.propertyName(), property.valueId(), property.valueName())).toList(), value.status());
    }

    /** 转换SKU分页响应。 */
    public static ProductSkuPageRes skuPage(ProductSkuDTO value) {
        return new ProductSkuPageRes(value.id(), value.spuId(), value.code(), value.name(), value.price(),
            value.stock(),
            value.image(), value.status());
    }

    /** 转换品牌分页。 */
    public static BrandPageRes brand(BrandDTO value) {
        return new BrandPageRes(value.id(), value.name(), value.picUrl(), value.description(), value.sort(),
            value.status());
    }

    /** 转换品牌选择。 */
    public static BrandSimpleRes brandSimple(BrandDTO value) {
        return new BrandSimpleRes(value.id(), value.name());
    }

    /** 转换分类树。 */
    public static CategoryTreeRes tree(CategoryTreeDTO value) {
        return new CategoryTreeRes(value.id(), value.name(), value.picUrl(), value.sort(), value.status(),
            value.children().stream().map(ProductManagementResConverter::tree).toList());
    }

    /** 转换商品状态统计。 */
    public static ProductSpuCountRes count(ProductSpuStatusCountDTO value) {
        return new ProductSpuCountRes(value.onShelf(), value.offShelf(), value.recycled());
    }

    /** 转换上架商品选择。 */
    public static ProductSpuSimpleRes simple(ProductSpuDTO value) {
        return new ProductSpuSimpleRes(value.id(), value.code(), value.name());
    }

    /** 转换批量商品详情。 */
    public static ProductSpuBatchRes batch(ProductSpuDTO value) {
        return new ProductSpuBatchRes(value.id(), value.code(), value.name(), value.categoryId(), value.brandId(),
            value.description(), value.image(), value.status(), value.skus().stream().map(ProductManagementResConverter::batchSku).toList(), value.deliveryTypes(), value.deliveryTemplateId());
    }

    /** 批量接口的SKU独立响应。 */
    public static ProductSpuBatchSkuRes batchSku(ProductSkuDTO value) {
        return new ProductSpuBatchSkuRes(value.id(), value.spuId(), value.code(), value.name(), value.price(),
            value.stock(), value.image(),
                value.properties() == null ? null : value.properties().stream().map(property ->
                        new ProductSpuBatchSkuRes.Property(property.propertyId(), property.propertyName(), property.valueId(), property.valueName())).toList(), value.status(), value.weight(), value.volume());
    }

}
