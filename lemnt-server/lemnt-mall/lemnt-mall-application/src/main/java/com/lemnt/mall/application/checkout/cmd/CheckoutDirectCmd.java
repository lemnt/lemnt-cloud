package com.lemnt.mall.application.checkout.cmd;

import java.util.List;

/**
 * 交易订单创建命令。
 */
public record CheckoutDirectCmd(
        Long memberId,
        List<OrderLine> items,
        String requestNo,
        Long couponId
) implements java.io.Serializable {
    /**
     * 创建交易订单创建命令实例。
     */
    public CheckoutDirectCmd(Long memberId, List<OrderLine> items) {
        this(memberId, items, null, null);
    }

    /**
     * 订单明细。
     */
    public record OrderLine(
            Long productId,
            Integer quantity
    ) {}
}
