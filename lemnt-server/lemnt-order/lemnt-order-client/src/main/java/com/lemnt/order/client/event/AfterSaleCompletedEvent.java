package com.lemnt.order.client.event;

import java.io.Serializable;
import java.math.BigDecimal;

/** 订单服务校验后产生的冻结业务事实，不包含当前配置及个人敏感资料。 */
public record AfterSaleCompletedEvent(
        String orderNo,
        String afterSaleNo,
        Long memberId,
        Long orderItemId,
        Long skuId,
        int quantity,
        BigDecimal amount,
        long returnUsePoint,
        long deductGivePoint,
        int ruleVersion,
        boolean fullyRefunded)
        implements Serializable {}
