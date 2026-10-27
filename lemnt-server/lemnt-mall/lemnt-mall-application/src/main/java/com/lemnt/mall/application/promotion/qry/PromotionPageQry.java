package com.lemnt.mall.application.promotion.qry;

/**
 * 营销分页查询。
 */
public record PromotionPageQry(
        int pageNum,
        int pageSize,
        String state,
        String code
) implements java.io.Serializable {}
