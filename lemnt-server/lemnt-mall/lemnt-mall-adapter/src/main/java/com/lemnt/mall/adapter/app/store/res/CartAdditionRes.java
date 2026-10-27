package com.lemnt.mall.adapter.app.store.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 购物车增量加购响应。
 */
@Schema(description = "CartAdditionRes响应")
public record CartAdditionRes(
        @Schema(description = "原请求编号")
        String requestNo,
        @Schema(description = "SKU编号")
        Long productId,
        @Schema(description = "本次增加数量")
        int addedQuantity,
        @Schema(description = "首次成功后的数量快照，不代表当前购物车")
        int resultingQuantity
) implements Serializable {}
