package com.lemnt.mall.adapter.web.product.converter;

import com.lemnt.mall.adapter.web.product.req.ProductFavoritePageReq;
import com.lemnt.mall.application.product.qry.ProductFavoritePageQry;

/** 商品收藏管理查询转换。 */
public final class ProductFavoriteReqConverter {
    private ProductFavoriteReqConverter() {}
    /** 保留false筛选及默认分页。 */
    public static ProductFavoritePageQry page(ProductFavoritePageReq req) {
        return new ProductFavoritePageQry(req.userId(), req.spuId(), req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }
}
