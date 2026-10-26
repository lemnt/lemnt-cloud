package com.lemnt.trade.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonInclude;

@Schema(description = "PromotionScopeRes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PromotionScopeRes(
        @Schema(description = "范围类型") String kind,
        @Schema(description = "静态ID，ALL为空") java.util.List<String> ids)
        implements java.io.Serializable {}
