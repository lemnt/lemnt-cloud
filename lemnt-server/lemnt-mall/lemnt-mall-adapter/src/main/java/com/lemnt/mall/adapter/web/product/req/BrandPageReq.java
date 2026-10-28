package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 品牌分页请求。 */
@Schema(description = "品牌分页请求")
public record BrandPageReq(
        @Schema(description = "名称关键词，可空")
        @Size(max = 100)
        String name,
        @Schema(description = "ENABLED或DISABLED")
        @Pattern(regexp = "ENABLED|DISABLED")
        String status,
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
