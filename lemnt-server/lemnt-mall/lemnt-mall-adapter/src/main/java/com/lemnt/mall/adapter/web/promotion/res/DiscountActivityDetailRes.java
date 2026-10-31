package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

/** 折扣活动详情响应。 */
@Schema(description = "折扣活动详情响应")
public record DiscountActivityDetailRes(
        @Schema(description = "活动编号")
        Long id,
        @Schema(description = "活动名称")
        String name,
        @Schema(description = "0 开启，1 关闭")
        Integer status,
        @Schema(description = "开始时间，yyyy-MM-dd HH:mm:ss")
        LocalDateTime startTime,
        @Schema(description = "结束时间，yyyy-MM-dd HH:mm:ss")
        LocalDateTime endTime,
        @Schema(description = "备注")
        String remark,
        @Schema(description = "SKU 折扣配置")
        List<DiscountProductRes> products
) implements java.io.Serializable {
}
