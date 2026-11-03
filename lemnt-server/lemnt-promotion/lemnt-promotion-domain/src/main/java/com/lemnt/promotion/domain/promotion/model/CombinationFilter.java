package com.lemnt.promotion.domain.promotion.model;

/** 拼团活动分页条件。 */
public record CombinationFilter(
        String name, Integer status, Long spuId, int pageNum, int pageSize) {}
