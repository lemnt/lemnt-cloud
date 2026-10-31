package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/** App 秒杀活动详情。 */
@Schema(description = "App 秒杀活动详情")
public record AppSeckillDetailRes(
        @Schema(description = "活动编号")
        Long id,
        @Schema(description = "活动名称")
        String name,
        @Schema(description = "0 开启，1 关闭")
        Integer status,
        @Schema(description = "当天开始时间")
        LocalDateTime startTime,
        @Schema(description = "当天结束时间")
        LocalDateTime endTime,
        @Schema(description = "商品 SPU 编号")
        Long spuId,
        @Schema(description = "总限购数量")
        Integer totalLimitCount,
        @Schema(description = "单次限购数量")
        Integer singleLimitCount,
        @Schema(description = "剩余活动库存")
        Integer stock,
        @Schema(description = "活动总库存")
        Integer totalStock,
        @Schema(description = "秒杀商品")
        List<AppSeckillProductRes> products
) implements java.io.Serializable {
}
