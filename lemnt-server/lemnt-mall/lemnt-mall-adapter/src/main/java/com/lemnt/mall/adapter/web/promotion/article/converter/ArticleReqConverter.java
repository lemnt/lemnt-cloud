package com.lemnt.mall.adapter.web.promotion.article.converter;

import com.lemnt.mall.adapter.web.promotion.article.req.*;
import com.lemnt.mall.application.promotion.article.cmd.*;
import com.lemnt.mall.application.promotion.article.qry.ArticlePageQry;

/** 商城文章HTTP请求转换。 */
public final class ArticleReqConverter {
    private ArticleReqConverter() {}
    public static ArticleCreateCmd create(ArticleCreateReq req) {
        return new ArticleCreateCmd(req.categoryId(), req.spuId(), req.title(), req.author(), req.picUrl(), req.introduction(), req.sort(), req.status(), req.recommendHot(), req.recommendBanner(), req.content());
    }
    public static ArticleUpdateCmd update(Long id, ArticleUpdateReq req) {
        return new ArticleUpdateCmd(id, req.categoryId(), req.spuId(), req.title(), req.author(), req.picUrl(), req.introduction(), req.sort(), req.status(), req.recommendHot(), req.recommendBanner(), req.content());
    }
    public static ArticlePageQry page(ArticlePageReq req) {
        return new ArticlePageQry(req.categoryId(), req.spuId(), req.title(), req.author(), req.status(), req.recommendHot(), req.recommendBanner(), req.createFrom(), req.createTo(), req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }
}
