package com.lemnt.promotion.application.article.cmd;

/** 商城内容管理输入。 */
public record ArticleSaveCmd(
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
        String content) {}
