package com.lemnt.mall.application.promotion.article.converter;

import com.lemnt.mall.domain.promotion.article.model.ArticleCategory;
import com.lemnt.mall.application.promotion.article.dto.ArticleCategoryDTO;

/** 文章分类领域结果转换。 */
public final class ArticleCategoryDTOConverter {
    private ArticleCategoryDTOConverter() {}
    public static ArticleCategoryDTO toDTO(ArticleCategory value) {
        return value == null ? null : new ArticleCategoryDTO(value.id(), value.name(), value.picUrl(), value.status(), value.sort(), value.createTime());
    }
}
