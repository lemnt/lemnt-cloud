package com.lemnt.mall.application.promotion.qry;

/** 砍价助力分页查询。 */
public record BargainHelpPageQry(Long recordId, Long userId, Long activityId, Integer pageNum,
        Integer pageSize) {}
