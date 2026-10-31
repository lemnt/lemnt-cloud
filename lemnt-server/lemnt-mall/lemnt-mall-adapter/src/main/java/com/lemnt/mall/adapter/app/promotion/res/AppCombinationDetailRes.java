package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** App 拼团活动详情。 */
@Schema(description = "App 拼团活动详情")
public record AppCombinationDetailRes(
        @Schema(description = "活动编号")
        Long id,
        @Schema(description = "活动名称")
        String name,
        @Schema(description = "0 开启，1 关闭")
        Integer status,
        @Schema(description = "开始时间")
        LocalDateTime startTime,
        @Schema(description = "结束时间")
        LocalDateTime endTime,
        @Schema(description = "成团人数")
        Integer userSize,
        @Schema(description = "成功团数量")
        Long successCount,
        @Schema(description = "商品 SPU 编号")
        Long spuId,
        @Schema(description = "总限购数量")
        Integer totalLimitCount,
        @Schema(description = "单次限购数量")
        Integer singleLimitCount,
        @Schema(description = "拼团商品")
        List<Product> products
) {
    /** 拼团商品。 */
    public record Product(Long skuId, BigDecimal combinationPrice) {}
}
