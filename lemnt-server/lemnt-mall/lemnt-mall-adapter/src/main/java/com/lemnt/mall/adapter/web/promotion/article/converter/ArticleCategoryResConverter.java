package com.lemnt.mall.adapter.web.promotion.article.converter;

import com.lemnt.mall.adapter.web.promotion.article.res.*;
import com.lemnt.mall.application.promotion.article.dto.ArticleCategoryDTO;

/** 文章分类独立HTTP响应转换。 */
public final class ArticleCategoryResConverter {
    private ArticleCategoryResConverter() {}
    public static ArticleCategoryPageRes page(ArticleCategoryDTO value) {
        return new ArticleCategoryPageRes(value.id(), value.name(), value.picUrl(), value.status(), value.sort(), value.createTime());
    }
    public static ArticleCategoryDetailRes detail(ArticleCategoryDTO value) {
        return new ArticleCategoryDetailRes(value.id(), value.name(), value.picUrl(), value.status(), value.sort(), value.createTime());
    }
    public static ArticleCategoryEnabledListRes enabled(ArticleCategoryDTO value) {
        return new ArticleCategoryEnabledListRes(value.id(), value.name());
    }

}
