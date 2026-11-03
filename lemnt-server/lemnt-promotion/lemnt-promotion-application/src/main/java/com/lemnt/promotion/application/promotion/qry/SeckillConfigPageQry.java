package com.lemnt.promotion.application.promotion.qry;

/** 秒杀时段分页查询。 */
public record SeckillConfigPageQry(
        String name, Integer status, Integer pageNum, Integer pageSize) {}
