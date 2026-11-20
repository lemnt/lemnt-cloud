package com.lemnt.promotion.application.article.qry;

/** 文章分类分页查询条件。 */
public record ArticleCategoryPageQry(String name, Integer status, int pageNum, int pageSize) {}
