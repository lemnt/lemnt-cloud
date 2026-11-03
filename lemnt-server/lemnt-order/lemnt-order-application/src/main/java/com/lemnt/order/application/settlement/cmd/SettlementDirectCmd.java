package com.lemnt.order.application.settlement.cmd;

import java.io.Serializable;
import java.util.List;

/**
 * 交易订单创建命令。
 */
public record SettlementDirectCmd(
        Long memberId, List<OrderLine> items, String requestNo, Long couponId)
        implements Serializable {
    /**
     * 创建交易订单创建命令实例。
     */
    public SettlementDirectCmd(Long memberId, List<OrderLine> items) {
        this(memberId, items, null, null);
    }

    /**
     * 订单明细。
     */
    public record OrderLine(Long productId, Integer quantity) {}
}
