package com.lemnt.promotion.adapter.web.article.converter;

import com.lemnt.promotion.adapter.web.article.req.ArticleCategoryPageReq;
import com.lemnt.promotion.adapter.web.article.req.ArticleCategorySaveReq;
import com.lemnt.promotion.adapter.web.article.req.ArticlePageReq;
import com.lemnt.promotion.adapter.web.article.req.ArticleSaveReq;
import com.lemnt.promotion.adapter.web.article.res.ArticleCategoryRes;
import com.lemnt.promotion.adapter.web.article.res.ArticleRes;
import com.lemnt.promotion.application.article.cmd.ArticleCategorySaveCmd;
import com.lemnt.promotion.application.article.cmd.ArticleSaveCmd;
import com.lemnt.promotion.application.article.dto.ArticleCategoryDTO;
import com.lemnt.promotion.application.article.dto.ArticleDTO;
import com.lemnt.promotion.application.article.qry.ArticleCategoryPageQry;
import com.lemnt.promotion.application.article.qry.ArticlePageQry;

public final class ArticleConverter {
    private ArticleConverter() {}

    public static ArticleSaveCmd save(Long id, ArticleSaveReq v) {
        return new ArticleSaveCmd(
                id,
                v.categoryId(),
                v.spuId(),
                v.title(),
                v.author(),
                v.picUrl(),
                v.introduction(),
                v.sort(),
                v.status(),
                v.recommendHot(),
                v.recommendBanner(),
                v.content());
    }

    public static ArticleCategorySaveCmd saveCategory(Long id, ArticleCategorySaveReq v) {
        return new ArticleCategorySaveCmd(id, v.name(), v.picUrl(), v.sort(), v.status());
    }

    public static ArticlePageQry page(ArticlePageReq v) {
        return new ArticlePageQry(
                v.title(),
                v.categoryId(),
                v.status(),
                v.recommendHot(),
                v.recommendBanner(),
                v.pageNum() == null ? 1 : v.pageNum(),
                v.pageSize() == null ? 20 : v.pageSize());
    }

    public static ArticleCategoryPageQry categoryPage(ArticleCategoryPageReq v) {
        return new ArticleCategoryPageQry(
                v.name(),
                v.status(),
                v.pageNum() == null ? 1 : v.pageNum(),
                v.pageSize() == null ? 20 : v.pageSize());
    }

    public static ArticleRes article(ArticleDTO v) {
        return v == null
                ? null
                : new ArticleRes(
                        v.id(),
                        v.categoryId(),
                        v.spuId(),
                        v.title(),
                        v.author(),
                        v.picUrl(),
                        v.introduction(),
                        v.sort(),
                        v.status(),
                        v.recommendHot(),
                        v.recommendBanner(),
                        v.content(),
                        v.browseCount());
    }

    public static ArticleCategoryRes category(ArticleCategoryDTO v) {
        return v == null
                ? null
                : new ArticleCategoryRes(v.id(), v.name(), v.picUrl(), v.sort(), v.status());
    }
}
