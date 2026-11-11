package com.lemnt.product.adapter.app.product.converter;

import com.lemnt.product.adapter.app.product.req.ProductCommentPageReq;
import com.lemnt.product.application.product.qry.ProductCommentPublicPageQry;

import java.util.List;

/**
 * 会员评价请求转换，会员身份来自网关。
 */
public final class ProductCommentReqConverter {
    private ProductCommentReqConverter() {
    }


    /**
     * App只绑定公开分页合同，显隐固定在领域存储条件中。
     */
    public static ProductCommentPublicPageQry page(ProductCommentPageReq req) {
        return new ProductCommentPublicPageQry(
            req.spuId(),
            req.type(),
            req.pageNum() == null ? 1 : req.pageNum(),
            req.pageSize() == null ? 20 : req.pageSize());
    }
}
