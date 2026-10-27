package com.lemnt.mall.adapter.web.promotion.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 营销错误响应。
 */
@Schema(description = "PromotionErrorRes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PromotionErrorRes(
        @Schema(description = "稳定错误码")
        String errorCode,
        @Schema(description = "是否可重试")
        boolean retryable,
        @Schema(description = "调用方处理动作")
        String action,
        @Schema(description = "服务端生成的关联编号")
        String correlationId
) implements Serializable {}
