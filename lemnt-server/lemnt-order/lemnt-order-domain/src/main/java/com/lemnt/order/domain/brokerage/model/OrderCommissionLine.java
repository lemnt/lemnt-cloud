package com.lemnt.order.domain.brokerage.model;

import java.math.BigDecimal;

/** 独立分佣订单的行快照。固定配置为空时该行使用全局比例。 */
public record OrderCommissionLine(
        Long orderId,
        Long orderItemId,
        FixedCommission fixed,
        BigDecimal firstEarned,
        BigDecimal secondEarned) {
    /** 实际入账值。未入账与明确发放零金额有不同含义。 */
    public BigDecimal earned(int level) {
        return switch (level) {
            case 1 -> firstEarned;
            case 2 -> secondEarned;
            default -> throw new IllegalArgumentException("佣金等级无效");
        };
    }
}
