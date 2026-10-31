package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/** 秒杀活动新增请求。 */
@Schema(description = "秒杀活动新增请求")
public record SeckillActivityCreateReq(
        @Schema(description = "商品 SPU 编号")
        @NotNull
        Long spuId,
        @Schema(description = "活动名称")
        @NotBlank @Size(max = 100)
        String name,
        @Schema(description = "备注")
        @Size(max = 500)
        String remark,
        @Schema(description = "开始时间，yyyy-MM-dd HH:mm:ss")
        @NotNull
        LocalDateTime startTime,
        @Schema(description = "结束时间，yyyy-MM-dd HH:mm:ss")
        @NotNull
        LocalDateTime endTime,
        @Schema(description = "排序")
        @NotNull
        Integer sort,
        @Schema(description = "秒杀时段编号数组")
        @NotEmpty
        List<Long> configIds,
        @Schema(description = "总限购数量")
        @NotNull
        Integer totalLimitCount,
        @Schema(description = "单次限购数量")
        @NotNull
        Integer singleLimitCount,
        @Schema(description = "秒杀商品")
        @NotEmpty @Valid
        List<SeckillProductReq> products
) implements java.io.Serializable {
}
