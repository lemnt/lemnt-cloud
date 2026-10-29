package com.lemnt.mall.adapter.web.promotion.article.converter;

import com.lemnt.mall.adapter.web.promotion.article.res.*;
import com.lemnt.mall.application.promotion.article.dto.ArticleDTO;

/** 商城文章独立HTTP响应转换。 */
public final class ArticleResConverter {
    private ArticleResConverter() {}
    public static ArticlePageRes page(ArticleDTO value) {
        return new ArticlePageRes(value.id(), value.categoryId(), value.spuId(), value.title(), value.author(), value.picUrl(), value.introduction(), value.sort(), value.status(), value.recommendHot(), value.recommendBanner(), value.content(), value.browseCount(), value.createTime());
    }
    public static ArticleDetailRes detail(ArticleDTO value) {
        return new ArticleDetailRes(value.id(), value.categoryId(), value.spuId(), value.title(), value.author(), value.picUrl(), value.introduction(), value.sort(), value.status(), value.recommendHot(), value.recommendBanner(), value.content(), value.browseCount(), value.createTime());
    }

}
