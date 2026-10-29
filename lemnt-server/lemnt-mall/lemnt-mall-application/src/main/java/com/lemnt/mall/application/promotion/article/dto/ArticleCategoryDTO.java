package com.lemnt.mall.application.promotion.article.dto;
/** 文章分类应用查询结果。 */
public record ArticleCategoryDTO(
        Long id,
        String name,
        String picUrl,
        Integer status,
        Integer sort,
        java.time.LocalDateTime createTime
) {
}
