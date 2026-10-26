package com.lemnt.trade.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "PromotionDetailRes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PromotionDetailRes(
        @Schema(description = "活动ID") String id,
        @Schema(description = "当前修订号") int revision,
        @Schema(description = "持久状态") String state,
        @Schema(description = "当前已发布版本，可空") Integer activeVersion,
        @Schema(description = "不可变活动编码") String code,
        @Schema(description = "可编辑草稿") PromotionDraftRes draft,
        @Schema(description = "当前发布快照，可空") PromotionDraftRes activeRule,
        @Schema(description = "UPCOMING/RUNNING/ENDED，可空") String timePhase)
        implements java.io.Serializable {}
