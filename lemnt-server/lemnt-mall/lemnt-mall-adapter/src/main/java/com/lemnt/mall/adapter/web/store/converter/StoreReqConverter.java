package com.lemnt.mall.adapter.web.store.converter;

public final class StoreReqConverter {
    private StoreReqConverter() {}

    public static com.lemnt.mall.application.store.qry.StorePageQry page(
            com.lemnt.mall.adapter.web.store.req.StorePageReq r) {
        return new com.lemnt.mall.application.store.qry.StorePageQry(
                r.name(),
                r.categoryId(),
                r.pageNum() == null ? 1 : r.pageNum(),
                r.pageSize() == null ? 20 : r.pageSize());
    }
}
