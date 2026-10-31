package com.lemnt.trade.adapter.app.aftersale.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 会员售后详情/列表项。 */
@Schema(description = "会员售后")
public record AppAfterSaleRes(
        @Schema(description = "售后单编号")
        String refundNo,
        @Schema(description = "订单编号")
        String orderNo,
        @Schema(description = "请求编号")
        String requestNo,
        @Schema(description = "原支付单编号")
        String paymentNo,
        @Schema(description = "商品行ID")
        Long orderItemId,
        @Schema(description = "售后数量")
        Integer quantity,
        @Schema(description = "方式：REFUND_ONLY/RETURN_REFUND")
        String type,
        @Schema(description = "退款金额，元")
        BigDecimal amount,
        @Schema(description = "申请原因")
        String reason,
        @Schema(description = "售后状态")
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
        @Schema(description = "退款成功时间")
        LocalDateTime refundTime,
        @Schema(description = "SPU编号")
        Long spuId,
        @Schema(description = "SPU名称")
        String spuName,
        @Schema(description = "规格快照")
        List<Property> properties,
        @Schema(description = "商品图片")
        String picUrl
) implements Serializable {
    public AppAfterSaleRes {
        properties = properties == null ? null : List.copyOf(properties);
    }

    /** 保存时的规格编号和名称。 */
    @Schema(description = "规格快照")
    public record Property(
            Long propertyId,
            String propertyName,
            Long valueId,
            String valueName
    ) implements Serializable {}
}
