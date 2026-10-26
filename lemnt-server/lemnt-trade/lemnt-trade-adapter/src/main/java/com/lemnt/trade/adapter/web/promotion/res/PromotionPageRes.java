package com.lemnt.trade.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "PromotionPageRes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PromotionPageRes(
        @Schema(description = "活动回执摘要") java.util.List<PromotionResultRes> records,
        @Schema(description = "非负整数总数") String total)
        implements java.io.Serializable {}
