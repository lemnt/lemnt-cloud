package com.lemnt.order.domain.promotion.model;

import java.util.Map;

/**
 * 支付前调价使用原赠送系数生成的头、行及优惠赠积分一致更新。
 */
public record OrderGiftAdjustment(
    Long orderId,
    Long promotionId,
    long givePoint,
    long promotionGivePoint,
    Map<Long, Long> itemGivePoints,
    Map<Long, Long> allocationGivePoints) {
}
