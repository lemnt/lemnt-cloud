package com.lemnt.mall.adapter.web.promotion.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 营销结果响应。
 */
@Schema(description = "MallPromotionSaveRes")
@JsonInclude(JsonInclude.Include.ALWAYS)
public record MallPromotionSaveRes(
        @Schema(description = "活动ID")
        String id,
        @Schema(description = "修订号，从0开始")
        int revision,
        @Schema(description = "持久状态")
        String state,
        @Schema(description = "已发布版本，可空")
        Integer activeVersion
) implements Serializable {}
