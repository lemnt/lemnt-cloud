package com.lemnt.trade.adapter.web.store.req;

@io.swagger.v3.oas.annotations.media.Schema(description = "CouponPublicClaimReq请求")
public record CouponPublicClaimReq(
        @io.swagger.v3.oas.annotations.media.Schema(description = "是否允许会员自行领取，默认关闭")
                @jakarta.validation.constraints.NotNull
                Boolean enabled)
        implements java.io.Serializable {}
