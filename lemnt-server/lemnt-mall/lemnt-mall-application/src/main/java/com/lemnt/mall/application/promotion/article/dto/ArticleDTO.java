package com.lemnt.mall.application.promotion.article.dto;
/** 商城文章应用查询结果。 */
public record ArticleDTO(
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
        Integer browseCount,
        java.time.LocalDateTime createTime
) {
}
