package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** App 拼团概要。 */
@Schema(description = "App 拼团概要")
public record AppCombinationSummaryRes(
        @Schema(description = "参团用户数")
        Long userCount,
        @Schema(description = "最近参团头像")
        List<String> avatars
) implements java.io.Serializable {
}
