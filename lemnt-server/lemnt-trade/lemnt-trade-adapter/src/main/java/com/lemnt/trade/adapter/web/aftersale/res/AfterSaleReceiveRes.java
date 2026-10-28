package com.lemnt.trade.adapter.web.aftersale.res;


import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 商品售后确认收货响应。 */
@Schema(description = "商品售后确认收货响应")
public record AfterSaleReceiveRes(
        @Schema(description = "售后单编号")
        String refundNo,
        @Schema(description = "订单编号")
        String orderNo,
        @Schema(description = "请求编号")
        String requestNo,
        @Schema(description = "会员ID")
        Long memberId,
        @Schema(description = "原支付单编号")
        String paymentNo,
        @Schema(description = "商品行ID，历史整单售后为空")
        Long orderItemId,
        @Schema(description = "售后商品数量，历史整单售后为空")
        Integer quantity,
        @Schema(description = "方式：REFUND_ONLY仅退款、RETURN_REFUND退货退款")
        String type,
        @Schema(description = "退款金额，单位元，由服务端计算")
        BigDecimal amount,
        @Schema(description = "申请原因")
        String reason,
        @Schema(description = "售后流程状态")
        String status,
        @Schema(description = "拒绝或拒收原因")
        String decisionReason,
        @Schema(description = "退货承运商")
        String returnCarrierCode,
        @Schema(description = "退货运单编号")
        String returnTrackingNo,
        @Schema(description = "申请时间")
        LocalDateTime requestedAt,
        @Schema(description = "审核时间")
        LocalDateTime decidedAt,
        @Schema(description = "退货登记时间")
        LocalDateTime returnShippedAt,
        @Schema(description = "退货处理时间")
        LocalDateTime returnReceivedAt,
        @Schema(description = "售后数值主键")
        Long id,
        @Schema(description = "订单数值主键")
        Long orderId,
        @Schema(description = "实际退款成功时间，历史无依据时为空")
        LocalDateTime refundTime,
        @Schema(description = "实际审批人员，历史无依据时为空")
        Long auditUserId,
        @Schema(description = "原订单SKU编号")
        Long skuId,
        @Schema(description = "商品SPU编号，历史未记录时为空")
        Long spuId,
        @Schema(description = "下单时商品SPU名称")
        String spuName,
        @Schema(description = "下单时规格名称和值，历史未记录与空规格区分")
        java.util.List<Property> properties,
        @Schema(description = "下单时商品图片")
        String picUrl
) implements Serializable {
    public AfterSaleReceiveRes {
        properties = properties == null ? null : java.util.List.copyOf(properties);
    }

    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public AfterSaleReceiveRes(
            String refundNo,
            String orderNo,
            String requestNo,
            Long memberId,
            String paymentNo,
            Long orderItemId,
            Integer quantity,
            String type,
            BigDecimal amount,
            String reason,
            String status,
            String decisionReason,
            String returnCarrierCode,
            String returnTrackingNo,
            LocalDateTime requestedAt,
            LocalDateTime decidedAt,
            LocalDateTime returnShippedAt,
            LocalDateTime returnReceivedAt,
            Long id,
            Long orderId,
            LocalDateTime refundTime,
            Long auditUserId
    ) {
        this(
                refundNo,
                orderNo,
                requestNo,
                memberId,
                paymentNo,
                orderItemId,
                quantity,
                type,
                amount,
                reason,
                status,
                decisionReason,
                returnCarrierCode,
                returnTrackingNo,
                requestedAt,
                decidedAt,
                returnShippedAt,
                returnReceivedAt,
                id,
                orderId,
                refundTime,
                auditUserId,
                null,
                null,
                null,
                null,
                null);
    }


    /** 保存时的规格编号和名称，历史缺失编号保持为空。 */
    @Schema(description = "商品规格快照")
    public record Property(
            @Schema(description = "规格项编号，历史未关联时为空")
            Long propertyId,
            @Schema(description = "保存时规格名称")
            String propertyName,
            @Schema(description = "规格值编号，历史未关联时为空")
            Long valueId,
            @Schema(description = "保存时规格值名称")
            String valueName
    ) implements java.io.Serializable {}
}
