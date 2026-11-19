package com.lemnt.promotion.application.article.cmd;

/** 商城内容管理输入。 */
public record ArticleCategorySaveCmd(
        Long id, String name, String picUrl, Integer sort, Integer status) {}
