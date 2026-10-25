package com.lemnt.mall.adapter.web.store.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "StoreDetail公开响应")
public record StoreDetailRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "id") Long id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "name") String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "categoryId") Long categoryId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "brandId") Long brandId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "description") String description,
        @io.swagger.v3.oas.annotations.media.Schema(description = "image") String image,
        @io.swagger.v3.oas.annotations.media.Schema(description = "skus")
                java.util.List<StoreSkuRes> skus)
        implements java.io.Serializable {}
