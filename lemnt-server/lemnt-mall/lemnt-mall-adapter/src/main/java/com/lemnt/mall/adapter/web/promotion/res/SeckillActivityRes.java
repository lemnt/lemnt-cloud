package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 秒杀活动详情及分页响应。 */
@Schema(description = "秒杀活动响应")
public record SeckillActivityRes(
        @Schema(description = "活动编号")
        Long id,
        @Schema(description = "商品 SPU 编号")
        Long spuId,
        @Schema(description = "商品名称")
        String spuName,
        @Schema(description = "商品主图")
        String picUrl,
        @Schema(description = "最低秒杀价")
        BigDecimal seckillPrice,
        @Schema(description = "活动名称")
        String name,
        @Schema(description = "0 开启，1 关闭")
        Integer status,
        @Schema(description = "备注")
        String remark,
        @Schema(description = "开始时间")
        LocalDateTime startTime,
        @Schema(description = "结束时间")
        LocalDateTime endTime,
        @Schema(description = "排序")
        Integer sort,
        @Schema(description = "秒杀时段编号数组")
        List<Long> configIds,
        @Schema(description = "总限购数量")
        Integer totalLimitCount,
        @Schema(description = "单次限购数量")
        Integer singleLimitCount,
        @Schema(description = "剩余活动库存")
        Integer stock,
        @Schema(description = "活动总库存")
        Integer totalStock,
        @Schema(description = "秒杀商品")
        List<SeckillProductRes> products
) implements java.io.Serializable {
}
