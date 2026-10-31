package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/** 秒杀时段修改请求。 */
@Schema(description = "秒杀时段修改请求")
public record SeckillConfigUpdateReq(
        @Schema(description = "时段名称")
        @NotBlank @Size(max = 100)
        String name,
        @Schema(description = "开始时间点，HH:mm:ss")
        @NotBlank
        String startTime,
        @Schema(description = "结束时间点，HH:mm:ss")
        @NotBlank
        String endTime,
        @Schema(description = "秒杀轮播图")
        List<String> sliderPicUrls,
        @Schema(description = "0 开启，1 关闭")
        @NotNull
        Integer status
) implements java.io.Serializable {
}
