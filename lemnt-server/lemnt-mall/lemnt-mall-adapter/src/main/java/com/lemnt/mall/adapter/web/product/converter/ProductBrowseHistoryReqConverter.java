package com.lemnt.mall.adapter.web.product.converter;

import com.lemnt.mall.adapter.web.product.req.ProductBrowseHistoryPageReq;
import com.lemnt.mall.application.product.qry.ProductBrowseHistoryPageQry;

/** 商品足迹管理查询转换。 */
public final class ProductBrowseHistoryReqConverter {
    private ProductBrowseHistoryReqConverter() {}
    /** 保留false筛选及默认分页。 */
    public static ProductBrowseHistoryPageQry page(ProductBrowseHistoryPageReq req) {
        return new ProductBrowseHistoryPageQry(req.userId(), req.spuId(), req.userDeleted(), req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }
}
