package com.lemnt.order.application.settlement.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 结算报价数据传输对象。
 */
public record SettlementQuoteDTO(
        List<SettlementItemDTO> items,
        BigDecimal originalAmount,
        BigDecimal discountAmount,
        BigDecimal payableAmount,
        Long couponId,
        String quoteToken,
        long usePoint,
        long givePoint,
        SettlementDeliveryDTO delivery,
        boolean freeDelivery,
        Map<Long, Integer> giveCouponTemplateCounts)
        implements Serializable {}
