package com.lemnt.mall.adapter.web.promotion.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 营销草稿响应。
 */
@Schema(description = "PromotionDraftRes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PromotionDraftRes(
        @Schema(description = "活动名")
        String name,
        @Schema(description = "FULL_REDUCTION或ORDER_DISCOUNT")
        String type,
        @Schema(description = "商品范围")
        PromotionScopeRes scope,
        @Schema(description = "ALL_AUTHENTICATED")
        String audience,
        @Schema(description = "EXCLUSIVE或ALLOW_ONE")
        String couponPolicy,
        @Schema(description = "同价优先级")
        int priority,
        @Schema(description = "UTC epoch毫秒，含起点")
        String startsAtEpochMs,
        @Schema(description = "UTC epoch毫秒，不含终点")
        String endsAtEpochMs,
        @Schema(description = "全局次数，可空")
        String globalUseLimit,
        @Schema(description = "每人次数，可空")
        String memberUseLimit,
        @Schema(description = "对应类型的规则")
        PromotionRuleRes rule
) implements Serializable {}
