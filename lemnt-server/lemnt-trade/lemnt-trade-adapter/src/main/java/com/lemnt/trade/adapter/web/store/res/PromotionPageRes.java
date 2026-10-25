package com.lemnt.trade.adapter.web.store.res;

@io.swagger.v3.oas.annotations.media.Schema(description = "PromotionPageRes响应")
public record PromotionPageRes(
        @io.swagger.v3.oas.annotations.media.Schema(description = "当前页")
                java.util.List<CouponOfferRes> records,
        @io.swagger.v3.oas.annotations.media.Schema(description = "总数") long total)
        implements java.io.Serializable {}
