package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 营销草稿请求。
 */
@Schema(description = "PromotionDraftReq")
public record PromotionDraftReq(
        @Schema(description = "活动名称，不含首尾空格")
        @NotBlank
        @Size(max = 100)
        String name,
        @Schema(description = "活动类型")
        @NotNull
        @Pattern(regexp = "FULL_REDUCTION|ORDER_DISCOUNT")
        String type,
        @Schema(description = "商品范围")
        @NotNull
        @Valid
        PromotionScopeReq scope,
        @Schema(description = "首期仅有效登录会员")
        @NotNull
        @Pattern(regexp = "ALL_AUTHENTICATED")
        String audience,
        @Schema(description = "是否允许一张指定券")
        @NotNull
        @Pattern(regexp = "EXCLUSIVE|ALLOW_ONE")
        String couponPolicy,
        @Schema(description = "同价优先级")
        @NotNull
        @Min(0)
        @Max(1000)
        Integer priority,
        @Schema(description = "UTC epoch毫秒，含起点")
        @NotNull
        @Pattern(regexp = "^[1-9][0-9]{0,18}$")
        String startsAtEpochMs,
        @Schema(description = "UTC epoch毫秒，不含终点")
        @NotNull
        @Pattern(regexp = "^[1-9][0-9]{0,18}$")
        String endsAtEpochMs,
        @Schema(description = "全局次数，null无限")
        @Pattern(regexp = "^[1-9][0-9]{0,18}$")
        String globalUseLimit,
        @Schema(description = "每人次数，null无限")
        @Pattern(regexp = "^[1-9][0-9]{0,18}$")
        String memberUseLimit,
        @Schema(description = "类型对应的严格规则")
        @NotNull
        @Valid
        PromotionRuleReq rule
) implements Serializable {}
