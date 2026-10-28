package com.lemnt.mall.adapter.web.coupon.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券模板创建请求。
 */
@Schema(description = "CouponTemplateCreateReq")
public record CouponTemplateCreateReq(
        @Schema(description = "模板编码")
        @NotBlank
        @Size(max = 64)
        String code,
        @Schema(description = "优惠券名称")
        @NotBlank
        @Size(max = 100)
        String name,
        @Schema(description = "FIXED满减或PERCENT折扣")
        @NotBlank
        @Pattern(regexp = "FIXED|PERCENT")
        String kind,
        @Schema(description = "最低商品金额，元")
        @NotNull
        @DecimalMin("0")
        @Digits(integer = 17, fraction = 2)
        BigDecimal threshold,
        @Schema(description = "满减金额，元，仅FIXED填写")
        @DecimalMin("0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal fixedAmount,
        @Schema(description = "支付百分比，80表示8折，仅PERCENT填写")
        @Min(1)
        @Max(100)
        Integer discountPercent,
        @Schema(description = "折扣金额上限，仅PERCENT可填写")
        @DecimalMin("0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal discountLimit,
        @Schema(description = "总发放数量，-1不限")
        @NotNull
        @Min(-1)
        Integer totalCount,
        @Schema(description = "数据库同一时区，yyyy-MM-dd HH:mm:ss")
        LocalDateTime validFrom,
        @Schema(description = "有效期右开边界，yyyy-MM-dd HH:mm:ss")
        LocalDateTime validUntil,
        @Schema(description = "使用说明")
        @Size(max = 500)
        String description,
        @Schema(description = "领取类型 USER、ADMIN、REGISTER")
        String takeType,
        @Schema(description = "每人限领数量，-1不限")
        Integer takeLimitCount,
        @Schema(description = "适用范围 ALL、SPU_IDS、CATEGORY_IDS")
        String productScope,
        @Schema(description = "适用SPU或分类编号")
        @Size(max = 1000)
        List<@NotNull @jakarta.validation.constraints.Positive Long> productScopeValues,
        @Schema(description = "有效期 DATE固定日期、TERM领取后生效")
        String validityType,
        @Schema(description = "领取后生效天数")
        Integer fixedStartTerm,
        @Schema(description = "生效后有效天数")
        Integer fixedEndTerm
) implements Serializable {}
