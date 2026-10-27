package com.lemnt.mall.adapter.app.store.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Map;

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
        @Schema(description = "规格属性名到值的映射")
        Map<String,
        String> properties
) implements Serializable {}
