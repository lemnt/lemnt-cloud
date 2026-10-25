package com.lemnt.mall.adapter.web.store.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "小程序商品查询")
public record StorePageReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "商品名称包含筛选")
                @jakarta.validation.constraints.Size(max = 200)
                String name,
        @io.swagger.v3.oas.annotations.media.Schema(description = "分类或父分类ID")
                @jakarta.validation.constraints.Positive
                Long categoryId,
        @io.swagger.v3.oas.annotations.media.Schema(description = "页码，默认1")
                @jakarta.validation.constraints.Min(1)
                @jakarta.validation.constraints.Max(1000000)
                Integer pageNum,
        @io.swagger.v3.oas.annotations.media.Schema(description = "每页条数，默认20，最大100")
                @jakarta.validation.constraints.Min(1)
                @jakarta.validation.constraints.Max(100)
                Integer pageSize)
        implements java.io.Serializable {}
