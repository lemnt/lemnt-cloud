package com.lemnt.order.client.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/** 原订单积分和金额，仅为可信消费者核对冻结权益。 */
public record OrderBenefitsSnapshotDTO(
        String orderNo,
        Long memberId,
        Long usePoint,
        Long givePoint,
        BigDecimal amount,
        String status)
        implements Serializable {}
