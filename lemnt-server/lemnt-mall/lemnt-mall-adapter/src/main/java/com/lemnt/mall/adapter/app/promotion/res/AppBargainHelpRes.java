package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

/** App 砍价助力。 */
@Schema(description = "App 砍价助力")
public record AppBargainHelpRes(
        @Schema(description = "助力编号")
        Long id,
        @Schema(description = "助力会员编号")
        Long userId,
        @Schema(description = "本次砍掉金额")
        BigDecimal reducePrice
) implements java.io.Serializable {
}
