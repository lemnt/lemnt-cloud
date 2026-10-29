package com.lemnt.mall.domain.promotion.article.model;
/** 文章分类领域记录，不包含存储注解。 */
public record ArticleCategory(
        Long id,
        String name,
        String picUrl,
        Integer status,
        Integer sort,
        java.time.LocalDateTime createTime
) {
}
