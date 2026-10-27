package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;

/**
 * 营销状态修改请求。
 */
@Schema(description = "PromotionStateChangeReq")
public record PromotionStateChangeReq(
        @Schema(description = "管理员幂等编号")
        @NotNull
        @Pattern(regexp = "^[A-Za-z0-9][A-Za-z0-9_-]{0,63}$")
        String requestNo,
        @Schema(description = "预期修订号")
        @NotNull
        @Min(0)
        Integer expectedRevision,
        @Schema(description = "暂停、恢复或归档")
        @NotNull
        @Pattern(regexp = "PAUSE|RESUME|ARCHIVE")
        String action
) implements Serializable {}
