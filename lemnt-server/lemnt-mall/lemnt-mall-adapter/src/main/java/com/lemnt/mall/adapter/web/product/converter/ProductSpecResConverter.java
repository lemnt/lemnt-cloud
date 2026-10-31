package com.lemnt.mall.adapter.web.product.converter;

import com.lemnt.mall.adapter.web.product.res.ProductSpecCreateRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpecDetailRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpecPageRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpecSimpleRes;
import com.lemnt.mall.adapter.web.product.res.ProductSpecUpdateRes;
import com.lemnt.mall.application.product.dto.ProductSpecDTO;

/** 规格字典独立接口响应转换。 */
public final class ProductSpecResConverter {
    private ProductSpecResConverter() {
    }

    /** 转换新增接口响应。 */
    public static ProductSpecCreateRes create(ProductSpecDTO value) {
        return new ProductSpecCreateRes(value.id(), value.name(), value.remark());
    }

    /** 转换修改接口响应。 */
    public static ProductSpecUpdateRes update(ProductSpecDTO value) {
        return new ProductSpecUpdateRes(value.id(), value.name(), value.remark());
    }

    /** 转换详情接口响应。 */
    public static ProductSpecDetailRes detail(ProductSpecDTO value) {
        return new ProductSpecDetailRes(value.id(), value.name(), value.remark(), value.createTime());
    }

    /** 转换分页接口响应。 */
    public static ProductSpecPageRes page(ProductSpecDTO value) {
        return new ProductSpecPageRes(value.id(), value.name(), value.remark(), value.createTime());
    }

    /** 转换Simple接口响应。 */
    public static ProductSpecSimpleRes simple(ProductSpecDTO value) {
        return new ProductSpecSimpleRes(value.id(), value.name());
    }
}
