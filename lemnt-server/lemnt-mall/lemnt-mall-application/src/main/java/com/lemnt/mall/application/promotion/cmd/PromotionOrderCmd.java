package com.lemnt.mall.application.promotion.cmd;

/** 活动订单统一下单命令。 */
public record PromotionOrderCmd(
        Long memberId,
        String activityType,
        Long activityId,
        Long recordId,
        Long headId,
        Long skuId,
        Integer quantity,
        Long addressId,
        String requestNo
) {}
