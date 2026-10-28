package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 启用品牌选择请求。 */
@Schema(description = "启用品牌选择请求")
public record BrandSimpleReq(
        @Schema(description = "名称关键词，可空")
        @Size(max = 100)
        String name,
        @Schema(description = "选择条数，默认20")
        @Min(1)
        @Max(100)
        Integer limit
) implements Serializable {
}
