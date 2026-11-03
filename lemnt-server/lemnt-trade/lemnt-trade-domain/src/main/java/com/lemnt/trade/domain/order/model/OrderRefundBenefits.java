package com.lemnt.trade.domain.order.model;

/** 退单生成时冻结、售后完成后执行的权益方案。 */
public record OrderRefundBenefits(
        String orderNo,
        String afterSaleNo,
        Long orderItemId,
        Long skuId,
        int quantity,
        java.math.BigDecimal amount,
        long returnUsePoint,
        long deductGivePoint,
        int ruleVersion) {}
