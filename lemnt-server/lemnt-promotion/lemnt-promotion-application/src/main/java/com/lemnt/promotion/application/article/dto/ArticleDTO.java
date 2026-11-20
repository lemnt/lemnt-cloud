package com.lemnt.promotion.application.article.dto;

import java.io.Serializable;

/** 商城内容查询结果。 */
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
        Long browseCount)
        implements Serializable {}
