package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 规格字典选择请求。 */
@Schema(description = "规格字典选择请求")
public record ProductSpecSimpleReq(
        @Schema(description = "名称关键词，可空")
        @Size(max = 64)
        String name,
        @Schema(description = "选择条数，默认20，最多100")
        @Min(1)
        @Max(100)
        Integer limit
) implements Serializable {
}
