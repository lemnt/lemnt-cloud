package com.lemnt.mall.adapter.app.settlement.res;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 结算回执响应。
 */
@Schema(description = "CheckoutReceipt响应")
public record SettlementReceiptRes(
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
        List<SettlementItemRes> items,
        @Schema(description = "已冻结的配送快照")
        SettlementDeliveryRes delivery,
        @Schema(description = "订单是否全部已评价，未知为空") Boolean commentStatus,
        @Schema(description = "全部评价完成时间，未确认时为空") LocalDateTime finishTime
) implements Serializable {
    /** 兼容报价/历史快照，订单项和评价元数据由持久化后读取。 */
    public SettlementReceiptRes(
            String orderNo,
            Long memberId,
            String status,
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            Long couponId,
            List<SettlementItemRes> items,
            SettlementDeliveryRes delivery
    ) {
        this(orderNo, memberId, status, originalAmount, discountAmount, payableAmount, couponId, items, delivery, null, null);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public SettlementReceiptRes(
            String orderNo,
            Long memberId,
            String status,
            BigDecimal originalAmount,
            BigDecimal discountAmount,
            BigDecimal payableAmount,
            Long couponId,
            List<SettlementItemRes> items) {
        this(orderNo, memberId, status, originalAmount, discountAmount, payableAmount, couponId, items, null);
    }
}
