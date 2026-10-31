package com.lemnt.mall.adapter.web.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/** 积分商城活动响应。 */
@Schema(description = "积分商城活动响应")
public record PointActivityRes(
        @Schema(description = "活动编号")
        Long id,
        @Schema(description = "商品 SPU 编号")
        Long spuId,
        @Schema(description = "商品名称")
        String spuName,
        @Schema(description = "商品主图")
        String picUrl,
        @Schema(description = "0 开启，1 关闭")
        Integer status,
        @Schema(description = "备注")
        String remark,
        @Schema(description = "排序")
        Integer sort,
        @Schema(description = "剩余库存")
        Integer stock,
        @Schema(description = "总库存")
        Integer totalStock,
        @Schema(description = "最低所需积分")
        Integer point,
        @Schema(description = "最低补差金额")
        BigDecimal price,
        @Schema(description = "商品市场价")
        BigDecimal marketPrice,
        @Schema(description = "积分商品")
        List<PointProductRes> products
) implements java.io.Serializable {
}
