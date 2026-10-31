package com.lemnt.mall.adapter.app.cart.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车明细响应。
 */
@Schema(description = "CartItem响应")
public record MallAppCartSaveRes(
        @Schema(description = "编号")
        Long id,
        @Schema(description = "SKU编号，沿用现有商品ID")
        Long productId,
        @Schema(description = "商品数量")
        int quantity,
        @Schema(description = "是否选中")
        boolean selected,
        @Schema(description = "购物车行版本")
        long version,
        @Schema(description = "名称，不可售商品可省略")
        String name,
        @Schema(description = "SKU编码，不可售商品可省略")
        String productCode,
        @Schema(description = "商品单价，元，JSON字符串")
        BigDecimal unitPrice,
        @Schema(description = "当前可用库存，不保证结算成功")
        Integer availableStock,
        @Schema(description = "当前SKU可售标识")
        boolean sellable
) implements Serializable {}
