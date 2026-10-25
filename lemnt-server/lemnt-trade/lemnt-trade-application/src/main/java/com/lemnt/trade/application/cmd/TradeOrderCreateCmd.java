package com.lemnt.trade.application.cmd;

import java.util.List;

public record TradeOrderCreateCmd(
        Long memberId, List<OrderLine> items, String requestNo, Long couponId) {
    public TradeOrderCreateCmd(Long memberId, List<OrderLine> items) {
        this(memberId, items, null, null);
    }

    public record OrderLine(Long productId, Integer quantity) {}
}
