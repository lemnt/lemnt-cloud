package com.lemnt.promotion.application.article.qry;

/** 分页查询条件。 */
public record ArticlePageQry(
        String title,
        Long categoryId,
        Integer status,
        Boolean recommendHot,
        Boolean recommendBanner,
        int pageNum,
        int pageSize) {}
