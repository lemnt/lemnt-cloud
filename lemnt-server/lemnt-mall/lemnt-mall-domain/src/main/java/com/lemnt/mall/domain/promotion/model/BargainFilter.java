package com.lemnt.mall.domain.promotion.model;

/** 砍价活动分页条件。 */
public record BargainFilter(String name, Integer status, int pageNum, int pageSize) {}
