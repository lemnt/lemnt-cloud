package com.lemnt.mall.application.promotion.qry;

/** 活动分页查询。 */
public record DiscountActivityPageQry(String name, Integer status, int pageNum, int pageSize) {}
