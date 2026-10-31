package com.lemnt.mall.domain.promotion.model;

/** 积分商城活动分页条件。 */
public record PointFilter(String name, Integer status, int pageNum, int pageSize) {}
