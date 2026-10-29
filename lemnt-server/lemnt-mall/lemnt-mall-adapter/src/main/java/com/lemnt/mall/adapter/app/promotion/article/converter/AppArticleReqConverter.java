package com.lemnt.mall.adapter.app.promotion.article.converter;

import com.lemnt.mall.adapter.app.promotion.article.req.*;
import com.lemnt.mall.application.promotion.article.qry.*;

/** App只开放源公开查询条件，不能伪造后台筛选。 */
public final class AppArticleReqConverter {
    private AppArticleReqConverter() {}
    public static ArticlePageQry page(AppArticlePageReq req) {
        return new ArticlePageQry(req.categoryId(), null, null, null, null, null, null, null, null,
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }
    public static ArticleReadQry detail(AppArticleDetailReq req) {
        return new ArticleReadQry(req.id(), req.title());
    }
    public static ArticleRecommendedQry recommended(AppArticleRecommendedListReq req) {
        return new ArticleRecommendedQry(req.recommendHot(), req.recommendBanner());
    }
}
