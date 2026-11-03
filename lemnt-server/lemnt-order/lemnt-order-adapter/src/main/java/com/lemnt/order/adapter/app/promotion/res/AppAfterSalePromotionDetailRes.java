package com.lemnt.order.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 优惠金额及权益快照响应。 */
@Schema(description = "AppAfterSalePromotionDetailRes优惠快照")
public record AppAfterSalePromotionDetailRes(
        @Schema(description = "售后业务单号") String afterSaleNo,
        @Schema(description = "原订单商品明细编号") Long orderItemId,
        @Schema(description = "本次售后数量") Integer quantity,
        @Schema(description = "本次冻结退款现金金额，单位元") BigDecimal amount,
        @Schema(description = "商品优惠金额或本次归因，单位元，不叠加退款") BigDecimal discountAmount,
        @Schema(description = "运费优惠金额或本次归因，单位元") BigDecimal deliveryDiscountPrice,
        @Schema(description = "本次应返使用积分，到账以账户执行为准") Long returnUsePoint,
        @Schema(description = "本次应回收赠送积分") Long deductGivePoint,
        @Schema(description = "生成退单时采用的分摊规则版本") Integer ruleVersion,
        @Schema(description = "快照状态：COMPLETE完整、LEGACY_PARTIAL历史不完整") String snapshotStatus,
        @Schema(description = "权益执行状态：NOT_STARTED、PENDING、SUCCESS、FAILED、UNKNOWN")
                String benefitStatus,
        @Schema(description = "实际采用的优惠或本次优惠归因") List<Promotion> promotions)
        implements Serializable {
    @Schema(description = "Promotion优惠快照")
    public record Promotion(
            @Schema(description = "优惠来源类型") String promotionType,
            @Schema(description = "下单时优惠名称快照") String promotionName,
            @Schema(description = "商品优惠金额或本次归因，单位元，不叠加退款") BigDecimal discountAmount,
            @Schema(description = "运费优惠金额或本次归因，单位元") BigDecimal deliveryDiscountPrice,
            @Schema(description = "本次应返使用积分，到账以账户执行为准") Long returnUsePoint,
            @Schema(description = "本次应回收赠送积分") Long deductGivePoint)
            implements Serializable {}
}
