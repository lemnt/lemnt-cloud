package com.lemnt.mall.adapter.app.promotion.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

/** App 积分商城活动。 */
@Schema(description = "App 积分商城活动")
public record AppPointActivityRes(
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
        List<Product> products
) {
    /** 积分商品。 */
    public record Product(Long skuId, Integer exchangeCount, Integer point, BigDecimal price, Integer stock) {}
}
