package com.lemnt.mall.adapter.web.store.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "StorePage公开响应")
public record StorePageRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "records")
                java.util.List<StoreProductRes> records,
        @io.swagger.v3.oas.annotations.media.Schema(description = "total") long total)
        implements java.io.Serializable {}
