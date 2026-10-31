package com.lemnt.mall.domain.promotion.model;

/** 秒杀活动分页条件。 */
public record SeckillFilter(String name, Integer status, Long configId, int pageNum, int pageSize) {}
