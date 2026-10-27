package com.lemnt.mall.adapter.app.store.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/**
 * 小程序商品查询。
 */
@Schema(description = "小程序商品查询")
public record StorePageReq(
        @Schema(description = "商品名称包含筛选")
        @Size(max = 200)
        String name,
        @Schema(description = "分类或父分类ID")
        @Positive
        Long categoryId,
        @Schema(description = "页码，默认1")
        @Min(1)
        @Max(1000000)
        Integer pageNum,
        @Schema(description = "每页条数，默认20，最大100")
        @Min(1)
        @Max(100)
        Integer pageSize
) implements Serializable {}
