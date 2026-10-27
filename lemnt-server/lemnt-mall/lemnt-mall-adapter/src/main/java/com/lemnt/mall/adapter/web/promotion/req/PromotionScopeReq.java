package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 营销范围请求。
 */
@Schema(description = "PromotionScopeReq")
public record PromotionScopeReq(
        @Schema(description = "范围类型")
        @NotNull
        @Pattern(regexp = "ALL|SPU_IDS|SKU_IDS")
        String kind,
        @Schema(description = "静态正整数ID，ALL为空")
        @NotNull
        @Size(max = 1000)
        List<@NotNull
        @Pattern(regexp = "^[1-9][0-9]{0,18}$")
        String> ids
) implements Serializable {}
