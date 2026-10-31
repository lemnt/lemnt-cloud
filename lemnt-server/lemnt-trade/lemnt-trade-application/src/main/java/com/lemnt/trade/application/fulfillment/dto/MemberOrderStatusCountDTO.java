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
        long refunded
) implements Serializable {
}
