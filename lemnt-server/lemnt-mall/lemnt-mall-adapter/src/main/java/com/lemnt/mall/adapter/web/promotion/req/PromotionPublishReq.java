package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 营销发布请求。
 */
@Schema(description = "PromotionPublishReq")
public record PromotionPublishReq(
        @Schema(description = "管理员幂等编号")
        @NotNull
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]{0,63}$")
        String requestNo,
        @Schema(description = "预期修订号")
        @NotNull
        @Min(0)
        Integer expectedRevision
) implements Serializable {}
