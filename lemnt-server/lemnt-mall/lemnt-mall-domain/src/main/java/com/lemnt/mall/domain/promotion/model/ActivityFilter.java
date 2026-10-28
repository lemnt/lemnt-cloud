package com.lemnt.mall.domain.promotion.model;

/** 活动分页条件。 */
public record ActivityFilter(String name, Integer status, int pageNum, int pageSize) {}
