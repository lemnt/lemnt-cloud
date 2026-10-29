package com.lemnt.mall.application.promotion.article.cmd;
/** 文章分类修改命令。 */
public record ArticleCategoryUpdateCmd(
        Long id,
        String name,
        String picUrl,
        Integer status,
        Integer sort
) {
}
