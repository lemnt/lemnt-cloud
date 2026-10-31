package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 秒杀时段精简响应。 */
@Schema(description = "秒杀时段精简响应")
public record SeckillConfigSimpleRes(
        @Schema(description = "时段编号")
        Long id,
        @Schema(description = "时段名称")
        String name,
        @Schema(description = "开始时间点")
        String startTime,
        @Schema(description = "结束时间点")
        String endTime,
        @Schema(description = "轮播图")
        List<String> sliderPicUrls
) implements java.io.Serializable {
}
