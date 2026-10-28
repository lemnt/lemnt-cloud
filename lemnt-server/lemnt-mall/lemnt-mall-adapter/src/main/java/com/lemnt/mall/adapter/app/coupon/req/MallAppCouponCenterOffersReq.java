package com.lemnt.mall.adapter.app.coupon.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.io.Serializable;

/**
 * 营销分页请求。
 */
@Schema(description = "PromotionPageReq请求")
public record MallAppCouponCenterOffersReq(
        @Schema(description = "页码默认1")
        @Min(1)
        @Max(1000000)
        Integer pageNum,
        @Schema(description = "每页默认20，最大100")
        @Min(1)
        @Max(100)
        Integer pageSize
) implements Serializable {}
