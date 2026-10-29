package com.lemnt.mall.domain.promotion.article.model;
/** 文章分类筛选，false/0保留为有效条件。 */
public record ArticleCategoryFilter(
        String name,
        Integer status,
        java.time.LocalDateTime createFrom,
        java.time.LocalDateTime createTo,
        int pageNum,
        int pageSize
) {
}
