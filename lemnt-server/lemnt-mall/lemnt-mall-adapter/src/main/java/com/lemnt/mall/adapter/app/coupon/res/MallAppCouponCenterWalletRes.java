package com.lemnt.mall.adapter.app.coupon.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 本人持券快照，模板修改或停用不影响已经发放的权益。 */
@Schema(description = "本人持券分页项")
public record MallAppCouponCenterWalletRes(
        @Schema(description = "本人持券编号") Long couponId,
        @Schema(description = "来源模板编号") Long templateId,
        @Schema(description = "AVAILABLE、LOCKED、USED、EXPIRED") String couponStatus,
        @Schema(description = "发放时冻结的规则") RuleRes terms,
        @Schema(description = "按状态及原有效期判断可用") boolean usable) implements Serializable {
    /** 此接口独立的持券规则响应。 */
    @Schema(name = "CouponWalletRuleRes", description = "本人持券规则快照")
    public record RuleRes(
            @Schema(description = "券编码")
            String code,
            @Schema(description = "券名称")
            String name,
            @Schema(description = "领取类型 USER、ADMIN、REGISTER")
            String takeType,
            @Schema(description = "FIXED满减、PERCENT折扣")
            String kind,
            @Schema(description = "适用商品金额门槛，元")
            BigDecimal threshold,
            @Schema(description = "满减金额，元")
            BigDecimal fixedAmount,
            @Schema(description = "支付百分比，80表示八折")
            Integer discountPercent,
            @Schema(description = "折扣优惠上限，元")
            BigDecimal discountLimit,
            @Schema(description = "适用范围 ALL、SPU_IDS、CATEGORY_IDS")
            String productScope,
            @Schema(description = "适用SPU或分类编号")
            List<Long> productScopeValues,
            @Schema(description = "原生效时间，yyyy-MM-dd HH:mm:ss")
            LocalDateTime validFrom,
            @Schema(description = "原到期时间，右开边界")
            LocalDateTime validUntil
    ) implements Serializable {
    }
}
