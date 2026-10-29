package com.lemnt.mall.application.promotion.article.converter;

import com.lemnt.mall.domain.promotion.article.model.Article;
import com.lemnt.mall.application.promotion.article.dto.ArticleDTO;

/** 商城文章领域结果转换。 */
public final class ArticleDTOConverter {
    private ArticleDTOConverter() {}
    public static ArticleDTO toDTO(Article value) {
        return value == null ? null : new ArticleDTO(value.id(), value.categoryId(), value.spuId(), value.title(), value.author(), value.picUrl(), value.introduction(), value.sort(), value.status(), value.recommendHot(), value.recommendBanner(), value.content(), value.browseCount(), value.createTime());
    }
}
