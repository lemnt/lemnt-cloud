package com.lemnt.mall.adapter.web.store.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "StoreProduct公开响应")
public record StoreProductRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "id") Long id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "name") String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "categoryId") Long categoryId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "brandId") Long brandId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "image") String image,
        @io.swagger.v3.oas.annotations.media.Schema(description = "最低售价（元，精确两位小数字符串）")
                String minPrice,
        @io.swagger.v3.oas.annotations.media.Schema(description = "最高售价（元，精确两位小数字符串）")
                String maxPrice,
        @io.swagger.v3.oas.annotations.media.Schema(description = "是否存在有库存的在售规格，结算时再次核验")
                boolean available)
        implements java.io.Serializable {}
