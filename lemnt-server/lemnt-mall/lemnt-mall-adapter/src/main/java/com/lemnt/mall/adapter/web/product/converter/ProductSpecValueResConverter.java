package com.lemnt.mall.adapter.web.product.converter;

import com.lemnt.mall.adapter.web.product.res.ProductSpecValueCreateRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpecValueDetailRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpecValuePageRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpecValueSimpleRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpecValueUpdateRes;
import com.lemnt.mall.application.product.dto.ProductSpecValueDTO;

/** 规格字典独立接口响应转换。 */
public final class ProductSpecValueResConverter {
    private ProductSpecValueResConverter() {
    }

    /** 转换新增接口响应。 */
    public static ProductSpecValueCreateRes create(ProductSpecValueDTO value) {
        return new ProductSpecValueCreateRes(value.id(), value.specId(), value.name(), value.remark());
    }

    /** 转换修改接口响应。 */
    public static ProductSpecValueUpdateRes update(ProductSpecValueDTO value) {
        return new ProductSpecValueUpdateRes(value.id(), value.specId(), value.name(), value.remark());
    }

    /** 转换详情接口响应。 */
    public static ProductSpecValueDetailRes detail(ProductSpecValueDTO value) {
        return new ProductSpecValueDetailRes(value.id(), value.specId(), value.name(), value.remark());
    }

    /** 转换分页接口响应。 */
    public static ProductSpecValuePageRes page(ProductSpecValueDTO value) {
        return new ProductSpecValuePageRes(value.id(), value.specId(), value.name(), value.remark());
    }

    /** 转换Simple接口响应。 */
    public static ProductSpecValueSimpleRes simple(ProductSpecValueDTO value) {
        return new ProductSpecValueSimpleRes(value.id(), value.specId(), value.name());
    }
}
