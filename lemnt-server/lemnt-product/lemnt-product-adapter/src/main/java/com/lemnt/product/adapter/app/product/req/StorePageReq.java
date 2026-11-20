package com.lemnt.product.adapter.app.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 小程序商品查询。
 */
@Schema(description = "小程序商品查询")
public record StorePageReq(
        @Schema(description = "商品名称包含筛选") @Size(max = 200) String name,
        @Schema(description = "分类或父分类ID") @Positive Long categoryId,
        @Schema(description = "页码，默认1") @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页条数，默认20，最大100") @Min(1) @Max(100) Integer pageSize,
        @Schema(description = "分类编号集合，与categoryId取并集并展开启用直接子分类，最多100个") @Size(max = 100)
                List<@NotNull @Positive Long> categoryIds,
        @Schema(description = "排序字段：price价格、salesCount展示销量、createTime真实创建时间（未知排在最后）；未传按运营排序")
                @Pattern(regexp = "price|salesCount|createTime")
                String sortField,
        @Schema(description = "升序，默认true；false表示降序") Boolean sortAsc)
        implements Serializable {}
