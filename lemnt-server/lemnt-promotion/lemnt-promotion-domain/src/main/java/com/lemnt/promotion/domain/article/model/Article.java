package com.lemnt.promotion.domain.article.model;

/** 商城内容管理资料。 */
public record Article(
        Long id,
        Long categoryId,
        Long spuId,
        String title,
        String author,
        String picUrl,
        String introduction,
        Integer sort,
        Integer status,
        Boolean recommendHot,
        Boolean recommendBanner,
        String content,
        Long browseCount) {}
