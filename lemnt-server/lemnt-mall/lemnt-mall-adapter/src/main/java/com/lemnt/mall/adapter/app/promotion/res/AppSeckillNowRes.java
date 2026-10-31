package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** App 当前秒杀时段及活动。 */
@Schema(description = "App 当前秒杀时段及活动")
public record AppSeckillNowRes(
        @Schema(description = "当前时段，无进行中时段时为空")
        AppSeckillConfigRes config,
        @Schema(description = "当前时段活动")
        List<AppSeckillActivityRes> activities
) implements java.io.Serializable {
}
