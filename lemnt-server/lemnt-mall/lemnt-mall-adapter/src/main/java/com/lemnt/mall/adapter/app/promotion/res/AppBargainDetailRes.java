package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** App 砍价详情。 */
@Schema(description = "App 砍价详情")
public record AppBargainDetailRes(
        @Schema(description = "砍价活动")
        AppBargainActivityRes activity,
        @Schema(description = "砍价记录")
        AppBargainRecordRes record,
        @Schema(description = "助力列表")
        List<AppBargainHelpRes> helps,
        @Schema(description = "本人是否已助力")
        Boolean helped
) implements java.io.Serializable {
}
