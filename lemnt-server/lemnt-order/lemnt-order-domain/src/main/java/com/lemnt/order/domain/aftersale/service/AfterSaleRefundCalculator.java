package com.lemnt.order.domain.aftersale.service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

/**
 * 仅在生成退单时调用，审批、渠道执行和重试使用已落库结果。
 */
public final class AfterSaleRefundCalculator {
    private AfterSaleRefundCalculator() {
    }

    /**
     * 原行实付除原数量向下取分；末次退完时结清原实付减已成功退款。
     */
    public static BigDecimal cash(
        BigDecimal original,
        int quantity,
        int refundedQuantity,
        BigDecimal refundedAmount,
        int requestedQuantity) {
        if (original == null
            || refundedAmount == null
            || original.signum() < 0
            || refundedAmount.signum() < 0
            || refundedAmount.compareTo(original) > 0
            || quantity < 1
            || refundedQuantity < 0
            || requestedQuantity < 1
            || requestedQuantity > quantity - refundedQuantity) {
            throw new IllegalArgumentException("原金额、已退额度或申请数量不合法");
        }
        var remaining = original.subtract(refundedAmount).setScale(2, RoundingMode.UNNECESSARY);
        var result =
            requestedQuantity == quantity - refundedQuantity
                ? remaining
                : original.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.DOWN)
                .multiply(BigDecimal.valueOf(requestedQuantity));
        if (result.compareTo(remaining) > 0) {
            throw new IllegalStateException("历史退款金额不一致，请先对账");
        }
        return result.setScale(2, RoundingMode.UNNECESSARY);
    }

    /**
     * 积分沿用累计比例增量，BigInteger避免先乘数量溢出。
     */
    public static long points(
        long original, int quantity, int refundedQuantity, int requestedQuantity) {
        if (original < 0
            || quantity < 1
            || refundedQuantity < 0
            || requestedQuantity < 1
            || requestedQuantity > quantity - refundedQuantity) {
            throw new IllegalArgumentException("积分退款数量不合法");
        }
        var total = BigInteger.valueOf(original);
        var q = BigInteger.valueOf(quantity);
        return total.multiply(BigInteger.valueOf((long) refundedQuantity + requestedQuantity))
            .divide(q)
            .subtract(total.multiply(BigInteger.valueOf(refundedQuantity)).divide(q))
            .longValueExact();
    }
}
