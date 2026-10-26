package com.lemnt.trade.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Schema(description = "PromotionCreateReq")
public record PromotionCreateReq(
        @Schema(description = "管理员幂等编号")
                @NotNull
                @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]{0,63}$")
                String requestNo,
        @Schema(description = "唯一大写活动编码") @NotNull @Pattern(regexp = "^[A-Z0-9][A-Z0-9_-]{0,63}$")
                String code,
        @Schema(description = "完整草稿") @NotNull @Valid PromotionDraftReq draft)
        implements java.io.Serializable {}
