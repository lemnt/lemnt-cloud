package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 商品SPU分页请求。
 */
@Schema(description = "ProductSpuPageReq")
public record ProductSpuPageReq(
        @Schema(description = "商品名称包含匹配")
        @Size(max = 200)
        String name,
        @Schema(description = "ON_SHELF、OFF_SHELF或RECYCLED，可不传")
        String status,
        @Schema(description = "页码，默认1")
        @Min(1)
        @Max(1000000)
        Integer pageNum,
        @Schema(description = "每页条数，默认10，最大100")
        @Min(1)
        @Max(100)
        Integer pageSize,
        @Schema(description = "精确分类ID，可不传")
        @jakarta.validation.constraints.Positive
        Long categoryId,
        @Schema(description = "精确品牌ID，可不传")
        @jakarta.validation.constraints.Positive
        Long brandId
) implements Serializable {
    /**
     * 创建商品SPU分页请求实例。
     */
    public ProductSpuPageReq {
        pageNum = pageNum == null ? 1 : pageNum;
        pageSize = pageSize == null ? 10 : pageSize;
    }
}
