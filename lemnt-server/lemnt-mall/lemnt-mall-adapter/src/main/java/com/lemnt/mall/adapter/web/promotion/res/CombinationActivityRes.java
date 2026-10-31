package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 拼团活动响应。 */
@Schema(description = "拼团活动响应")
public record CombinationActivityRes(
        @Schema(description = "活动编号")
        Long id,
        @Schema(description = "活动名称")
        String name,
        @Schema(description = "商品 SPU 编号")
        Long spuId,
        @Schema(description = "商品名称")
        String spuName,
        @Schema(description = "商品主图")
        String picUrl,
        @Schema(description = "最低拼团价")
        BigDecimal combinationPrice,
        @Schema(description = "总限购数量")
        Integer totalLimitCount,
        @Schema(description = "单次限购数量")
        Integer singleLimitCount,
        @Schema(description = "开始时间")
        LocalDateTime startTime,
        @Schema(description = "结束时间")
        LocalDateTime endTime,
        @Schema(description = "成团人数")
        Integer userSize,
        @Schema(description = "是否虚拟成团")
        Boolean virtualGroup,
        @Schema(description = "0 开启，1 关闭")
        Integer status,
        @Schema(description = "限制时长，小时")
        Integer limitDuration,
        @Schema(description = "拼团商品")
        List<CombinationProductRes> products
) implements java.io.Serializable {
}
