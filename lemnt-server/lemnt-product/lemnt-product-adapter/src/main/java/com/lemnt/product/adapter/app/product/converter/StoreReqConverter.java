package com.lemnt.product.adapter.app.product.converter;

import com.lemnt.product.adapter.app.product.req.StorePageReq;
import com.lemnt.product.application.product.qry.StorePageQry;

/**
 * 门店请求转换器。
 */
public final class StoreReqConverter {
    private StoreReqConverter() {
    }

    /**
     * 转换分页查询或分页响应。
     */
    public static StorePageQry page(StorePageReq r) {
        return new StorePageQry(
            r.name(),
            r.categoryId(),
            r.pageNum() == null ? 1 : r.pageNum(),
            r.pageSize() == null ? 20 : r.pageSize());
    }
}
