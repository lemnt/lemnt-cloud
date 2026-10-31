package com.lemnt.mall.application.promotion.qry;

/** 砍价记录分页查询。 */
public record BargainRecordPageQry(Long userId, Long activityId, Integer status, Integer pageNum,
        Integer pageSize) {}
