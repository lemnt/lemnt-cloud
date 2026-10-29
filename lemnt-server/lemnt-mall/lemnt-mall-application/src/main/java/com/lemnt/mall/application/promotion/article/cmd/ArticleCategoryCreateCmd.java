package com.lemnt.mall.application.promotion.article.cmd;
/** 文章分类新增命令。 */
public record ArticleCategoryCreateCmd(
        String name,
        String picUrl,
        Integer status,
        Integer sort
) {
}
