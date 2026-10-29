package com.lemnt.mall.adapter.web.promotion.article.converter;

import com.lemnt.mall.adapter.web.promotion.article.req.*;
import com.lemnt.mall.application.promotion.article.cmd.*;
import com.lemnt.mall.application.promotion.article.qry.ArticleCategoryPageQry;

/** 文章分类HTTP请求转换。 */
public final class ArticleCategoryReqConverter {
    private ArticleCategoryReqConverter() {}
    public static ArticleCategoryCreateCmd create(ArticleCategoryCreateReq req) {
        return new ArticleCategoryCreateCmd(req.name(), req.picUrl(), req.status(), req.sort());
    }
    public static ArticleCategoryUpdateCmd update(Long id, ArticleCategoryUpdateReq req) {
        return new ArticleCategoryUpdateCmd(id, req.name(), req.picUrl(), req.status(), req.sort());
    }
    public static ArticleCategoryPageQry page(ArticleCategoryPageReq req) {
        return new ArticleCategoryPageQry(req.name(), req.status(), req.createFrom(), req.createTo(), req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }
}
