package com.lemnt.mall.application.promotion.qry;

/** 拼团记录分页查询。 */
public record CombinationRecordPageQry(Long userId, Long activityId, Integer status, Long headId,
        Integer pageNum, Integer pageSize) {}
