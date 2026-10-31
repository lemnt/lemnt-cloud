package com.lemnt.trade.adapter.web.fulfillment.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单汇总响应。
 */
@Schema(description = "订单履约OrderSummaryRes")
public record OrderSummaryRes(
        @Schema(description = "订单编号")
        String orderNo,
        @Schema(description = "会员ID")
        Long memberId,
        @Schema(description = "应付金额，人民币元")
        BigDecimal totalAmount,
        @Schema(description = "订单状态：RESERVING/PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CLOSED/REFUNDED")
        String status,
        @Schema(description = "订单创建时间")
        LocalDateTime createTime,
        @Schema(description = "全部订单库存是否已确认")
        boolean inventoryConfirmed,
        @Schema(description = "是否有待处理退款申请")
        boolean activeRefund,
        @Schema(description = "承运商编码")
        String carrierCode,
        @Schema(description = "运单编号")
        String trackingNo,
        @Schema(description = "发货时间，数据库时间")
        LocalDateTime shippedAt,
        @Schema(description = "收货时间，未收货为空")
        LocalDateTime receivedAt
) implements Serializable {}
