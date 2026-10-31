package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

/** SKU上下架请求。 */
@Schema(description = "SKU上下架请求")
public record ProductSkuShelfReq(
        @Schema(description = "是否上架SKU")
        @NotNull
        Boolean onShelf
) implements Serializable {
}
