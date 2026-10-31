package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/** 商品批量详情请求。 */
@Schema(description = "商品批量详情请求")
public record ProductSpuBatchReq(
        @Schema(description = "1至100个商品ID")
        @NotEmpty @Size(max = 100)
        List<@NotNull
        @Positive Long> ids
) implements Serializable {
}
