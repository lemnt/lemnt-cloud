package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** 秒杀时段响应。 */
@Schema(description = "秒杀时段响应")
public record SeckillConfigRes(
        @Schema(description = "时段编号")
        Long id,
        @Schema(description = "时段名称")
        String name,
        @Schema(description = "开始时间点")
        String startTime,
        @Schema(description = "结束时间点")
        String endTime,
        @Schema(description = "轮播图")
        List<String> sliderPicUrls,
        @Schema(description = "0 开启，1 关闭")
        Integer status,
        @Schema(description = "关联活动数量")
        Integer activityCount
) implements java.io.Serializable {
}
