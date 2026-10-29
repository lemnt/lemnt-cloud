package com.lemnt.mall.application.promotion.article.cmd;
/** 商城文章修改命令。 */
public record ArticleUpdateCmd(
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
        String content
) {
}
