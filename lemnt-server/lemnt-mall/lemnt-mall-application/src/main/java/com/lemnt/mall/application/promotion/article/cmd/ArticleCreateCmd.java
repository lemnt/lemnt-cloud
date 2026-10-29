package com.lemnt.mall.application.promotion.article.cmd;
/** 商城文章新增命令。 */
public record ArticleCreateCmd(
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
        String content
) {
}
