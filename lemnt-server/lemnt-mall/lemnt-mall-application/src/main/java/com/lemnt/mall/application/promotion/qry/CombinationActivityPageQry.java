package com.lemnt.mall.application.promotion.qry;

/** 拼团活动分页查询。 */
public record CombinationActivityPageQry(String name, Integer status, Long spuId, Integer pageNum,
        Integer pageSize) {}
