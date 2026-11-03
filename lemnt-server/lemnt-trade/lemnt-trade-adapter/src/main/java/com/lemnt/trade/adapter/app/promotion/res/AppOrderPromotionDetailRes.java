package com.lemnt.trade.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** 优惠金额及权益快照响应。 */
@Schema(description = "AppOrderPromotionDetailRes优惠快照")
public record AppOrderPromotionDetailRes(
        @Schema(description = "订单业务单号") String orderNo,
        @Schema(description = "快照状态：COMPLETE完整、LEGACY_PARTIAL历史不完整") String snapshotStatus,
        @Schema(description = "订单冻结金额汇总") Summary summary,
        @Schema(description = "实际采用的优惠或本次优惠归因") List<Promotion> promotions)
        implements Serializable {
    @Schema(description = "Summary优惠快照")
    public record Summary(
            @Schema(description = "商品原价合计，单位元") BigDecimal originalAmount,
            @Schema(description = "商品优惠金额或本次归因，单位元，不叠加退款") BigDecimal discountAmount,
            @Schema(description = "优惠前运费，单位元") BigDecimal deliveryOriginalPrice,
            @Schema(description = "运费优惠金额或本次归因，单位元") BigDecimal deliveryDiscountPrice,
            @Schema(description = "人工调价差额，正数加价负数减价，单位元") BigDecimal adjustmentAmount,
            @Schema(description = "冻结应付金额，单位元") BigDecimal payAmount)
            implements Serializable {}

    @Schema(description = "Promotion优惠快照")
    public record Promotion(
            @Schema(description = "优惠来源类型") String promotionType,
            @Schema(description = "下单时优惠名称快照") String promotionName,
            @Schema(description = "商品优惠金额或本次归因，单位元，不叠加退款") BigDecimal discountAmount,
            @Schema(description = "运费优惠金额或本次归因，单位元") BigDecimal deliveryDiscountPrice,
            @Schema(description = "使用积分数量") Long usePoint,
            @Schema(description = "赠送积分数量") Long givePoint,
            @Schema(description = "赠券模板编号与数量映射") Map<Long, Integer> giveCouponTemplateCounts,
            @Schema(description = "原订单商品行优惠分摊") List<Item> items)
            implements Serializable {}

    @Schema(description = "Item优惠快照")
    public record Item(
            @Schema(description = "原订单商品明细编号") Long orderItemId,
            @Schema(description = "商品优惠金额或本次归因，单位元，不叠加退款") BigDecimal discountAmount,
            @Schema(description = "运费优惠金额或本次归因，单位元") BigDecimal deliveryDiscountPrice,
            @Schema(description = "使用积分数量") Long usePoint,
            @Schema(description = "赠送积分数量") Long givePoint)
            implements Serializable {}
}
