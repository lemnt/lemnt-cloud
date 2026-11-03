package com.lemnt.product.adapter.web.product.converter;

import com.lemnt.product.adapter.web.product.res.ProductBrowseHistoryPageRes;
import com.lemnt.product.application.product.dto.ProductBrowseHistoryDTO;

/** web BrowseHistory独立响应转换。 */
public final class ProductBrowseHistoryResConverter {
    private ProductBrowseHistoryResConverter() {}

    /** 只映射当前接口允许展示的字段。 */
    public static ProductBrowseHistoryPageRes page(ProductBrowseHistoryDTO value) {
        return new ProductBrowseHistoryPageRes(
                value.id(),
                value.spuId(),
                value.spuName(),
                value.picUrl(),
                value.price(),
                value.createTime(),
                value.userId(),
                value.userDeleted());
    }
}
