package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** 砍价助力响应。 */
@Schema(description = "砍价助力响应")
public record BargainHelpRes(
        @Schema(description = "助力编号")
        Long id,
        @Schema(description = "活动编号")
        Long activityId,
        @Schema(description = "砍价记录编号")
        Long recordId,
        @Schema(description = "助力会员编号")
        Long userId,
        @Schema(description = "本次砍掉金额")
        BigDecimal reducePrice
) implements java.io.Serializable {
}
