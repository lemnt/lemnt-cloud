package com.lemnt.mall.adapter.web.checkout.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券模板响应。
 */
@Schema(description = "CouponTemplate响应")
public record MallCouponTemplateTemplatesRes(
        @Schema(description = "编号")
        Long id,
        @Schema(description = "模板编码")
        String code,
        @Schema(description = "名称，不可售商品可省略")
        String name,
        @Schema(description = "FIXED或PERCENT")
        String kind,
        @Schema(description = "商品金额门槛，元")
        BigDecimal threshold,
        @Schema(description = "满减金额，元")
        BigDecimal fixedAmount,
        @Schema(description = "支付百分比，80表示8折")
        Integer discountPercent,
        @Schema(description = "折扣金额上限，元")
        BigDecimal discountLimit,
        @Schema(description = "总发放数量")
        int totalCount,
        @Schema(description = "已发放数量")
        int issuedCount,
        @Schema(description = "生效时间，yyyy-MM-dd HH:mm:ss")
        LocalDateTime validFrom,
        @Schema(description = "过期时间，右开边界")
        LocalDateTime validUntil,
        @Schema(description = "是否开放新发放及兑换")
        boolean enabled
) implements Serializable {}
