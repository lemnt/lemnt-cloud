package com.lemnt.mall.application.promotion.article.qry;
/** 推荐标记查询，null表示不筛选，false为有效值。 */
public record ArticleRecommendedQry(
        Boolean recommendHot,
        Boolean recommendBanner
) {
}
