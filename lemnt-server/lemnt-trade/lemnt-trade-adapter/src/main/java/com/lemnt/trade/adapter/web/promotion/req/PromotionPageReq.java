package com.lemnt.trade.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "PromotionPageReq")
public record PromotionPageReq(
        @Schema(description = "页码，默认1") @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页数量，默认20") @Min(1) @Max(100) Integer pageSize,
        @Schema(description = "可选精确状态") @Pattern(regexp = "DRAFT|PUBLISHED|PAUSED|ARCHIVED")
                String state,
        @Schema(description = "可选精确活动编码") @Pattern(regexp = "^[A-Z0-9][A-Z0-9_-]{0,63}$")
                String code)
        implements java.io.Serializable {}
