package com.lemnt.mall.adapter.app.product.converter;

import com.lemnt.mall.adapter.app.product.req.AppProductBrowseHistoryCreateReq;
import com.lemnt.mall.adapter.app.product.req.AppProductBrowseHistoryPageReq;
import com.lemnt.mall.application.product.cmd.ProductBrowseHistoryCreateCmd;
import com.lemnt.mall.application.product.qry.ProductBrowseHistoryPageQry;

/** AppBrowseHistory入参转换，会员身份只能从认证上下文传入。 */
public final class ProductBrowseHistoryReqConverter {
    private ProductBrowseHistoryReqConverter() {}
    /** 创建命令没有客户端会员字段。 */
    public static ProductBrowseHistoryCreateCmd create(Long member, AppProductBrowseHistoryCreateReq req) {
        return new ProductBrowseHistoryCreateCmd(member, req.spuId());
    }

    /** 分页固定本人范围。 */
    public static ProductBrowseHistoryPageQry page(Long member, AppProductBrowseHistoryPageReq req) {
        return new ProductBrowseHistoryPageQry(member, null, false, req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }
}
