package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** SKU分页请求。 */
@Schema(description = "SKU分页请求")
public record ProductSkuPageReq(
        @Schema(description = "SPU编号，可空")
        @Positive
        Long spuId,
        @Schema(description = "精确SKU编码")
        @Size(max = 64)
        String code,
        @Schema(description = "名称关键词")
        @Size(max = 200)
        String name,
        @Schema(description = "ON_SHELF或OFF_SHELF")
        @Pattern(regexp = "ON_SHELF|OFF_SHELF")
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
