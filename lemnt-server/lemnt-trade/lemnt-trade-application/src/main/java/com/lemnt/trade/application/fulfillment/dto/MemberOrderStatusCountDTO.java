package com.lemnt.trade.application.fulfillment.dto;

import java.io.Serializable;

/** 会员订单各状态数量，按订单中心分组展示。 */
public record MemberOrderStatusCountDTO(
        long pendingPayment,
        long awaitingShipment,
        long awaitingReceipt,
        long awaitingComment,
        long completed,
        long closed,
        long refunded,
        long awaitingPickup
) implements Serializable {
    /** 兼容未区分待自提的订单中心统计。 */
    public MemberOrderStatusCountDTO(long pendingPayment, long awaitingShipment, long awaitingReceipt,
            long awaitingComment, long completed, long closed, long refunded) {
        this(pendingPayment, awaitingShipment, awaitingReceipt, awaitingComment, completed, closed, refunded, 0L);
    }
}
