package com.lemnt.mall.application.promotion.cmd;

/** 创建拼团记录命令，由订单支付链路调用。 */
public record CombinationRecordCreateCmd(
        Long userId,
        Long spuId,
        Long activityId,
        Long headId,
        Long skuId,
        Integer count
) {}
