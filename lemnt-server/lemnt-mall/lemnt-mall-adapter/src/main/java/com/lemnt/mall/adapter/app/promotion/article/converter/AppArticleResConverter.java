package com.lemnt.mall.adapter.app.promotion.article.converter;

import com.lemnt.mall.adapter.app.promotion.article.res.*;
import com.lemnt.mall.application.promotion.article.dto.ArticleDTO;

/** App文章独立响应转换。 */
public final class AppArticleResConverter {
    private AppArticleResConverter() {}
    public static AppArticlePageRes page(ArticleDTO value) {
        return value == null ? null : new AppArticlePageRes(value.id(), value.title(), value.author(), value.categoryId(), value.picUrl(), value.introduction(), value.content(), value.createTime(), value.browseCount(), value.spuId());
    }
    public static AppArticleDetailRes detail(ArticleDTO value) {
        return value == null ? null : new AppArticleDetailRes(value.id(), value.title(), value.author(), value.categoryId(), value.picUrl(), value.introduction(), value.content(), value.createTime(), value.browseCount(), value.spuId());
    }
    public static AppArticleRecommendedListRes recommendedList(ArticleDTO value) {
        return value == null ? null : new AppArticleRecommendedListRes(value.id(), value.title(), value.author(), value.categoryId(), value.picUrl(), value.introduction(), value.content(), value.createTime(), value.browseCount(), value.spuId());
    }

}
