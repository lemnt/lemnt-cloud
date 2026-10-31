package com.lemnt.mall.domain.promotion.model;

/** 秒杀时段分页条件。 */
public record SeckillConfigFilter(String name, Integer status, int pageNum, int pageSize) {}
