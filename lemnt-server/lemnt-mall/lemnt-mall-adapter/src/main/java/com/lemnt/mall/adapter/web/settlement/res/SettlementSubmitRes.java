package com.lemnt.mall.adapter.web.settlement.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 结算回执响应。
 */
@Schema(description = "CheckoutReceipt响应")
public record SettlementSubmitRes(
        @Schema(description = "订单号")
        String orderNo,
        @Schema(description = "后台授权操作的目标会员编号，不是当前后台用户ID")
        Long memberId,
        @Schema(description = "订单状态：RESERVING/PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CLOSED")
        String status,
        @Schema(description = "商品原金额，元")
        BigDecimal originalAmount,
        @Schema(description = "优惠金额，元")
        BigDecimal discountAmount,
        @Schema(description = "实付金额，元")
        BigDecimal payableAmount,
        @Schema(description = "优惠券实例编号，未选择时省略")
        Long couponId,
        @Schema(description = "商品价格和数量快照")
        List<SettlementItemRes> items
) implements Serializable {}
