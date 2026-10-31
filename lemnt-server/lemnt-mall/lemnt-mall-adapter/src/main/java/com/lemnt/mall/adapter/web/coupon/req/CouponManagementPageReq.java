package com.lemnt.mall.adapter.web.coupon.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

/** 后台会员持券分页请求。 */
@Schema(description = "后台会员持券分页请求")
public record CouponManagementPageReq(
        @Schema(description = "会员编号") @Positive Long memberId,
        @Schema(description = "模板编号") @Positive Long templateId,
        @Schema(description = "持券状态") @Pattern(regexp = "AVAILABLE|LOCKED|USED|EXPIRED") String status,
        @Schema(description = "页码，默认1") @Min(1) @Max(1000000) Integer pageNum,
        @Schema(description = "每页条数，默认20") @Min(1) @Max(100) Integer pageSize) implements Serializable {
}
