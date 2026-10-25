package com.lemnt.trade.adapter.web.store.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "PromotionPageReq请求")
public record PromotionPageReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "页码默认1")
                @jakarta.validation.constraints.Min(1)
                @jakarta.validation.constraints.Max(1000000)
                Integer pageNum,
        @io.swagger.v3.oas.annotations.media.Schema(description = "每页默认20，最大100")
                @jakarta.validation.constraints.Min(1)
                @jakarta.validation.constraints.Max(100)
                Integer pageSize)
        implements java.io.Serializable {}
