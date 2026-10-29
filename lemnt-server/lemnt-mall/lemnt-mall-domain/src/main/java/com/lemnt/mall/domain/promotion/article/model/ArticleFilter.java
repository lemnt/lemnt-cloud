package com.lemnt.mall.domain.promotion.article.model;
/** 商城文章筛选，false/0保留为有效条件。 */
public record ArticleFilter(
        Long categoryId,
        Long spuId,
        String title,
        String author,
        Integer status,
        Boolean recommendHot,
        Boolean recommendBanner,
        java.time.LocalDateTime createFrom,
        java.time.LocalDateTime createTo,
        int pageNum,
        int pageSize
) {
}
