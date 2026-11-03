package com.lemnt.mall.client.bo;

import java.io.Serializable;
import java.math.BigDecimal;

/** 交易已完成售后的冻结方案，金额为元，积分为数量。 */
public record RefundBenefitsBo(
        String orderNo,
        String afterSaleNo,
        Long orderItemId,
        Long skuId,
        int quantity,
        BigDecimal amount,
        long returnUsePoint,
        long deductGivePoint,
        int ruleVersion)
        implements Serializable {}
