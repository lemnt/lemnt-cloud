package com.lemnt.trade.application.fulfillment.executor;

import com.lemnt.trade.application.fulfillment.dto.MemberOrderStatusCountDTO;
import com.lemnt.trade.domain.order.gateway.TradeOrderGateway;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/** 会员订单状态数量统计，区分待评价与已完成。 */
@Component
@RequiredArgsConstructor
public class MemberOrderStatusCountQueryExe {
    private final TradeOrderGateway orders;

    /** 一次分组查询后按订单中心分组归并。 */
    public MemberOrderStatusCountDTO execute(Long memberId) {
        if (memberId == null) {
            throw new IllegalArgumentException("会员身份缺失");
        }
        long pendingPayment = 0;
        long awaitingShipment = 0;
        long awaitingReceipt = 0;
        long awaitingComment = 0;
        long completed = 0;
        long closed = 0;
        long refunded = 0;
        for (var count : orders.memberStatusCounts(memberId)) {
            switch (count.status()) {
                case "RESERVING", "PENDING_PAYMENT" -> pendingPayment += count.count();
                case "PAID" -> awaitingShipment += count.count();
                case "SHIPPED" -> awaitingReceipt += count.count();
                case "COMPLETED" -> {
                    if (Boolean.TRUE.equals(count.commentStatus())) {
                        completed += count.count();
                    } else {
                        awaitingComment += count.count();
                    }
                }
                case "CLOSED" -> closed += count.count();
                case "REFUNDED" -> refunded += count.count();
                default -> {
                    // 未知状态不计入任何分组，保持原样等待对账。
                }
            }
        }
        return new MemberOrderStatusCountDTO(pendingPayment, awaitingShipment, awaitingReceipt,
                awaitingComment, completed, closed, refunded);
    }
}
