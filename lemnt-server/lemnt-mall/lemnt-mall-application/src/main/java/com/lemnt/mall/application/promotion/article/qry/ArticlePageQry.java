package com.lemnt.mall.application.promotion.article.qry;
/** 商城文章筛选，false/0保留为有效条件。 */
public record ArticlePageQry(
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
