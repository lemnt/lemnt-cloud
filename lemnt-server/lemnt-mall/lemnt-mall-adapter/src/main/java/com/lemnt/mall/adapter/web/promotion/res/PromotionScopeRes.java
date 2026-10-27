package com.lemnt.mall.adapter.web.promotion.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 营销范围响应。
 */
@Schema(description = "PromotionScopeRes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record PromotionScopeRes(
        @Schema(description = "范围类型")
        String kind,
        @Schema(description = "静态ID，ALL为空")
        List<String> ids
) implements Serializable {}
