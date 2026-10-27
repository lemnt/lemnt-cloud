package com.lemnt.mall.adapter.web.checkout.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 结算明细响应。
 */
@Schema(description = "CheckoutItem响应")
public record CheckoutItemRes(
        @Schema(description = "SKU编号，沿用现有商品ID")
        Long productId,
        @Schema(description = "SKU编码，不可售商品可省略")
        String productCode,
        @Schema(description = "商品名称快照")
        String productName,
        @Schema(description = "商品单价，元，JSON字符串")
        BigDecimal unitPrice,
        @Schema(description = "商品数量")
        Integer quantity,
        @Schema(description = "商品行原金额，元")
        BigDecimal lineAmount
) implements Serializable {}
