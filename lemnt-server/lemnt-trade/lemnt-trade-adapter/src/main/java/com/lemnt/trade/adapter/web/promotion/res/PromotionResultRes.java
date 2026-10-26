package com.lemnt.trade.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "PromotionResultRes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PromotionResultRes(
        @Schema(description = "活动ID") String id,
        @Schema(description = "修订号，从0开始") int revision,
        @Schema(description = "持久状态") String state,
        @Schema(description = "已发布版本，可空") Integer activeVersion)
        implements java.io.Serializable {}
