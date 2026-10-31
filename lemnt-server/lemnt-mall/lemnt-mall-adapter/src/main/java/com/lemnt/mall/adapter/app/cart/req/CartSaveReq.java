package com.lemnt.mall.adapter.app.cart.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 购物车保存请求。
 */
@Schema(description = "CartSaveReq")
public record CartSaveReq(
        @Schema(description = "购物数量，1至999")
        @NotNull
        @Min(1)
        @Max(999)
        Integer quantity,
        @Schema(description = "是否选中")
        @NotNull
        Boolean selected
) implements Serializable {}
