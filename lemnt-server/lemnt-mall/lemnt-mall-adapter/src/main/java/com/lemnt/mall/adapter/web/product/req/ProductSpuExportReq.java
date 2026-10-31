package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** 商品导出独立请求；忽略外部页码，只导出本次筛选的完整结果。 */
@Schema(description = "商品导出查询")
public record ProductSpuExportReq(
        @Schema(description = "商品名称关键词") @Size(max = 200) String name,
        @Schema(description = "商品状态，可空") @Size(max = 20) String status,
        @Schema(description = "精确分类ID") @Positive Long categoryId,
        @Schema(description = "精确品牌ID") @Positive Long brandId,
        @Schema(description = "库存分组SOLD_OUT或LOW_STOCK，可空") @Size(max = 20) String stockStatus
) implements Serializable {
}
