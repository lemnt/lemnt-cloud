package com.lemnt.mall.infrastructure.promotion.article.converter;

import com.lemnt.mall.domain.promotion.article.model.Article;
import com.lemnt.mall.infrastructure.promotion.article.dataobject.ArticleDO;

/** 商城文章存储转换。 */
public final class ArticleConverter {
    private ArticleConverter() {}
    public static Article toModel(ArticleDO row) {
        return row == null ? null : new Article(row.getId(), row.getCategoryId(), row.getSpuId(), row.getTitle(), row.getAuthor(), row.getPicUrl(), row.getIntroduction(), row.getSort(), row.getStatus(), row.getRecommendHot(), row.getRecommendBanner(), row.getContent(), row.getBrowseCount(), row.getCreateTime());
    }
    public static ArticleDO toDO(Article value) {
        var row = new ArticleDO();
        row.setId(value.id());
        row.setCategoryId(value.categoryId());
        row.setSpuId(value.spuId());
        row.setTitle(value.title());
        row.setAuthor(value.author());
        row.setPicUrl(value.picUrl());
        row.setIntroduction(value.introduction());
        row.setSort(value.sort());
        row.setStatus(value.status());
        row.setRecommendHot(value.recommendHot());
        row.setRecommendBanner(value.recommendBanner());
        row.setContent(value.content());
        row.setBrowseCount(value.browseCount());
        return row;
    }
}
