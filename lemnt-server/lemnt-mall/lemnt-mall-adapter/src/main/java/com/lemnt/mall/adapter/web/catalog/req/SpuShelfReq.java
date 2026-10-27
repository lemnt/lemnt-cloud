package com.lemnt.mall.adapter.web.catalog.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 商品商品SPU上架请求。
 */
@Schema(description = "SpuShelfReq")
public record SpuShelfReq(
        @Schema(description = "是否上架")
        @NotNull
        Boolean onShelf
) implements Serializable {}
