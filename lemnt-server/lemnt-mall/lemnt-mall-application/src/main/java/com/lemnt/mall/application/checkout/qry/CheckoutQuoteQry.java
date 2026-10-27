package com.lemnt.mall.application.checkout.qry;

import java.util.List;

/**
 * 结算报价查询。
 */
public record CheckoutQuoteQry(
        Long memberId,
        List<Long> productIds,
        Long couponId
) implements java.io.Serializable {}
