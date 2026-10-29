package com.lemnt.mall.application.promotion.article.qry;
/** App文章详情查询，id优先。 */
public record ArticleReadQry(
        Long id,
        String title
) {
}
