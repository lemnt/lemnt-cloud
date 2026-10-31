package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 上架商品选择请求。 */
@Schema(description = "上架商品选择请求")
public record ProductSpuSimpleReq(
        @Schema(description = "商品名称关键词")
        @Size(max = 200)
        String name,
        @Schema(description = "选择条数，默认20，最大100")
        @Min(1)
        @Max(100)
        Integer limit
) implements Serializable {
}
