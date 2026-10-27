package com.lemnt.mall.application.store.qry;

/**
 * 营销分页查询。
 */
public record PromotionPageQry(
        Long memberId,
        int pageNum,
        int pageSize
) implements java.io.Serializable {}
