package com.lemnt.promotion.domain.promotion.model;

/**
 * 装修模板分页条件。
 */
public record DiyTemplateFilter(String name, Boolean used, int pageNum, int pageSize) {
}
