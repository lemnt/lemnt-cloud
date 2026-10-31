package com.lemnt.mall.domain.promotion.model;

/** 砍价记录分页条件。 */
public record BargainRecordFilter(Long userId, Long activityId, Integer status, int pageNum, int pageSize) {}
