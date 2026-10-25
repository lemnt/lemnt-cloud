package com.lemnt.mall.adapter.web.store.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "StoreSku公开响应")
public record StoreSkuRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "id") Long id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "name") String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "当前售价（元，精确两位小数字符串）") String price,
        @io.swagger.v3.oas.annotations.media.Schema(description = "当前可用库存，不表示库存预留") int stock,
        @io.swagger.v3.oas.annotations.media.Schema(description = "image") String image,
        @io.swagger.v3.oas.annotations.media.Schema(description = "规格属性名到值的映射")
                java.util.Map<String, String> properties)
        implements java.io.Serializable {}
