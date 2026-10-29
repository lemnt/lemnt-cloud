package com.lemnt.mall.infrastructure.promotion.article.converter;

import com.lemnt.mall.domain.promotion.article.model.ArticleCategory;
import com.lemnt.mall.infrastructure.promotion.article.dataobject.ArticleCategoryDO;

/** 文章分类存储转换。 */
public final class ArticleCategoryConverter {
    private ArticleCategoryConverter() {}
    public static ArticleCategory toModel(ArticleCategoryDO row) {
        return row == null ? null : new ArticleCategory(row.getId(), row.getName(), row.getPicUrl(), row.getStatus(), row.getSort(), row.getCreateTime());
    }
    public static ArticleCategoryDO toDO(ArticleCategory value) {
        var row = new ArticleCategoryDO();
        row.setId(value.id());
        row.setName(value.name());
        row.setPicUrl(value.picUrl());
        row.setStatus(value.status());
        row.setSort(value.sort());
        return row;
    }
}
