package com.lemnt.promotion.domain.promotion.model;

/** 装修页面分页条件。 */
public record DiyPageFilter(Long templateId, String name, int pageNum, int pageSize) {}
