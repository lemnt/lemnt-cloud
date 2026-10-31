package com.lemnt.mall.domain.promotion.model;

/** 拼团记录分页条件。 */
public record CombinationRecordFilter(Long userId, Long activityId, Integer status, Long headId,
        int pageNum, int pageSize) {}
