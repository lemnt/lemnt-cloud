package com.lemnt.mall.application.promotion.article.qry;
/** 文章分类筛选，false/0保留为有效条件。 */
public record ArticleCategoryPageQry(
        String name,
        Integer status,
        java.time.LocalDateTime createFrom,
        java.time.LocalDateTime createTo,
        int pageNum,
        int pageSize
) {
}
