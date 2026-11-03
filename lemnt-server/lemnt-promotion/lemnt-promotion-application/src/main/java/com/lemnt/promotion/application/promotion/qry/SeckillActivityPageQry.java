package com.lemnt.promotion.application.promotion.qry;

/** 秒杀活动分页查询。 */
public record SeckillActivityPageQry(
        String name, Integer status, Long configId, Integer pageNum, Integer pageSize) {}
