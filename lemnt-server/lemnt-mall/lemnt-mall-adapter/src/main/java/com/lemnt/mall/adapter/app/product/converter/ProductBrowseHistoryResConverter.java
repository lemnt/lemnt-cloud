package com.lemnt.mall.adapter.app.product.converter;

import com.lemnt.mall.adapter.app.product.res.AppProductBrowseHistoryPageRes;
import com.lemnt.mall.application.product.dto.ProductBrowseHistoryDTO;

/** app BrowseHistory独立响应转换。 */
public final class ProductBrowseHistoryResConverter {
    private ProductBrowseHistoryResConverter() {}
    /** 只映射当前接口允许展示的字段。 */
    public static AppProductBrowseHistoryPageRes page(ProductBrowseHistoryDTO value) {
        return new AppProductBrowseHistoryPageRes(value.id(), value.spuId(), value.spuName(), value.picUrl(), value.price(), value.createTime());
    }
}
