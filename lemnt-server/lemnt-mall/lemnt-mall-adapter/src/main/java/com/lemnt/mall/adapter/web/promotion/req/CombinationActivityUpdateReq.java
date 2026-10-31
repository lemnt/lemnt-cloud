package com.lemnt.mall.adapter.web.promotion.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

/** 拼团活动修改请求。 */
@Schema(description = "拼团活动修改请求")
public record CombinationActivityUpdateReq(
        @Schema(description = "活动名称")
        @NotBlank @Size(max = 100)
        String name,
        @Schema(description = "商品 SPU 编号")
        @NotNull
        Long spuId,
        @Schema(description = "总限购数量")
        @NotNull
        Integer totalLimitCount,
        @Schema(description = "单次限购数量")
        @NotNull
        Integer singleLimitCount,
        @Schema(description = "开始时间，yyyy-MM-dd HH:mm:ss")
        @NotNull
        LocalDateTime startTime,
        @Schema(description = "结束时间，yyyy-MM-dd HH:mm:ss")
        @NotNull
        LocalDateTime endTime,
        @Schema(description = "成团人数")
        @NotNull
        Integer userSize,
        @Schema(description = "是否虚拟成团")
        @NotNull
        Boolean virtualGroup,
        @Schema(description = "限制时长，小时")
        @NotNull
        Integer limitDuration,
        @Schema(description = "拼团商品")
        @NotEmpty @Valid
        List<CombinationProductReq> products
) implements java.io.Serializable {
}
