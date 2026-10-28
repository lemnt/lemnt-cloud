package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 规格字典分页请求。 */
@Schema(description = "规格字典分页请求")
public record ProductSpecPageReq(
        @Schema(description = "名称关键词，可空")
        @Size(max = 64)
        String name,
        @Schema(description = "页码，默认1")
        @Min(1)
        @Max(1000000)
        Integer pageNum,
        @Schema(description = "每页数量，默认20，最多100")
        @Min(1)
        @Max(100)
        Integer pageSize
) implements Serializable {
}
