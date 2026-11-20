package com.lemnt.promotion.application.article.dto;

import java.io.Serializable;

/** 商城内容查询结果。 */
public record ArticleCategoryDTO(Long id, String name, String picUrl, Integer sort, Integer status)
        implements Serializable {}
