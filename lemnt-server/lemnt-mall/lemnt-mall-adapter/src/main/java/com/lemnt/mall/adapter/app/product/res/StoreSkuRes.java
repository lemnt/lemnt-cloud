package com.lemnt.mall.adapter.app.product.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.List;

/**
 * 门店商品SKU响应。
 */
@Schema(description = "StoreSku公开响应")
public record StoreSkuRes(
        @Schema(description = "id")
        Long id,
        @Schema(description = "name")
        String name,
        @Schema(description = "当前售价（元，精确两位小数字符串）")
        String price,
        @Schema(description = "当前可用库存，不表示库存预留")
        int stock,
        @Schema(description = "image")
        String image,
        @Schema(description = "规格对象列表")
        List<Property> properties
) implements Serializable {
    /** 编号与名称保持一一对应。 */
    @Schema(description = "商品规格")
    public record Property(
            @Schema(description = "规格项编号")
            Long propertyId,
            @Schema(description = "规格名称，写入时可省略")
            String propertyName,
            @Schema(description = "规格值编号")
            Long valueId,
            @Schema(description = "规格值名称，写入时可省略")
            String valueName
    ) implements java.io.Serializable {}
}
