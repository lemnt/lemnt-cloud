package com.lemnt.product.adapter.web.product.converter;

import com.lemnt.product.adapter.web.product.res.ProductFavoritePageRes;
import com.lemnt.product.application.product.dto.ProductFavoriteDTO;

/** web Favorite独立响应转换。 */
public final class ProductFavoriteResConverter {
    private ProductFavoriteResConverter() {}

    /** 只映射当前接口允许展示的字段。 */
    public static ProductFavoritePageRes page(ProductFavoriteDTO value) {
        return new ProductFavoritePageRes(
                value.id(),
                value.spuId(),
                value.spuName(),
                value.picUrl(),
                value.price(),
                value.userId(),
                value.createTime());
    }
}
