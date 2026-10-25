package com.lemnt.mall.adapter.web.store.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "StoreCategory公开响应")
public record StoreCategoryRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "id") Long id,
        @io.swagger.v3.oas.annotations.media.Schema(description = "parentId") Long parentId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "name") String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "picUrl") String picUrl)
        implements java.io.Serializable {}
