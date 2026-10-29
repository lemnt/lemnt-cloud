package com.lemnt.mall.domain.promotion.article.model;
/** 商城文章领域记录，不包含存储注解。 */
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
        Integer browseCount,
        java.time.LocalDateTime createTime
) {
}
