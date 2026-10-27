package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 营销草稿保存请求。
 */
@Schema(description = "PromotionDraftSaveReq")
public record PromotionDraftSaveReq(
        @Schema(description = "管理员幂等编号")
        @NotNull
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]{0,63}$")
        String requestNo,
        @Schema(description = "预期修订号，从0开始")
        @NotNull
        @Min(0)
        Integer expectedRevision,
        @Schema(description = "完整草稿")
        @NotNull
        @Valid
        PromotionDraftReq draft
) implements Serializable {}
