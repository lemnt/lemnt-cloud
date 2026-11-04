package com.lemnt.member.domain.growth.model;

import java.math.BigDecimal;

/** 用于核对原订单，缺失历史快照保持未知。 */
public record OrderBenefitsSnapshot(
        String orderNo,
        Long memberId,
        Long usePoint,
        Long givePoint,
        BigDecimal amount,
        String status) {}
