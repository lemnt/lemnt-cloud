package com.lemnt.mall.adapter.app.store.converter;

import com.lemnt.mall.adapter.app.store.req.StorePageReq;
import com.lemnt.mall.application.store.qry.StorePageQry;

/**
 * 门店请求转换器。
 */
public final class StoreReqConverter {
    private StoreReqConverter() {}

    /**
     * 转换分页查询或分页响应。
     */
    public static StorePageQry page(
            StorePageReq r) {
        return new StorePageQry(
                r.name(),
                r.categoryId(),
                r.pageNum() == null ? 1 : r.pageNum(),
                r.pageSize() == null ? 20 : r.pageSize());
    }
}
