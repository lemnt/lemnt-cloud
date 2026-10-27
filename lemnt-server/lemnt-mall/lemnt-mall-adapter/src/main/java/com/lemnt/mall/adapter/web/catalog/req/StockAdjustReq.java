package com.lemnt.mall.adapter.web.catalog.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 库存调整请求。
 */
@Schema(description = "StockAdjustReq")
public record StockAdjustReq(
        @Schema(description = "库存幂等请求编号")
        @NotBlank
        @Size(max = 64)
        String requestNo,
        @Schema(description = "库存增减量，非0，不能覆盖绝对值")
        @NotNull
        Integer delta,
        @Schema(description = "变更原因")
        @NotBlank
        @Size(max = 200)
        String reason
) implements Serializable {}
