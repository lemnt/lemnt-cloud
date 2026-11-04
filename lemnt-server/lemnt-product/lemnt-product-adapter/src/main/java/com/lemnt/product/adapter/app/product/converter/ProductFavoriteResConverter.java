package com.lemnt.product.adapter.app.product.converter;

import com.lemnt.product.adapter.app.product.res.AppProductFavoritePageRes;
import com.lemnt.product.application.product.dto.ProductFavoriteDTO;

/**
 * app Favorite独立响应转换。
 */
public final class ProductFavoriteResConverter {
    private ProductFavoriteResConverter() {
    }

    /**
     * 只映射当前接口允许展示的字段。
     */
    public static AppProductFavoritePageRes page(ProductFavoriteDTO value) {
        return new AppProductFavoritePageRes(
            value.id(), value.spuId(), value.spuName(), value.picUrl(), value.price());
    }
}
