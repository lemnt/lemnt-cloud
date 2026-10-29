package com.lemnt.mall.adapter.app.product.converter;

import com.lemnt.mall.adapter.app.product.req.AppProductFavoriteCreateReq;
import com.lemnt.mall.adapter.app.product.req.AppProductFavoritePageReq;
import com.lemnt.mall.application.product.cmd.ProductFavoriteCreateCmd;
import com.lemnt.mall.application.product.qry.ProductFavoritePageQry;

/** AppFavorite入参转换，会员身份只能从认证上下文传入。 */
public final class ProductFavoriteReqConverter {
    private ProductFavoriteReqConverter() {}
    /** 创建命令没有客户端会员字段。 */
    public static ProductFavoriteCreateCmd create(Long member, AppProductFavoriteCreateReq req) {
        return new ProductFavoriteCreateCmd(member, req.spuId());
    }

    /** 分页固定本人范围。 */
    public static ProductFavoritePageQry page(Long member, AppProductFavoritePageReq req) {
        return new ProductFavoritePageQry(member, null, req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }
}
