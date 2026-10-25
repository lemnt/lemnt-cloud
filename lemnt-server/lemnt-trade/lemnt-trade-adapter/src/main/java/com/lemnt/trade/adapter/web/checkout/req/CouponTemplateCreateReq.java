package com.lemnt.trade.adapter.web.checkout.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Schema(description = "CouponTemplateCreateReq")
public record CouponTemplateCreateReq(
        @Schema(description = "模板编码") @NotBlank @Size(max = 64) String code,
        @Schema(description = "优惠券名称") @NotBlank @Size(max = 100) String name,
        @Schema(description = "FIXED满减或PERCENT折扣") @NotBlank @Pattern(regexp = "FIXED|PERCENT")
                String kind,
        @Schema(description = "最低商品金额，元")
                @NotNull
                @DecimalMin("0")
                @Digits(integer = 17, fraction = 2)
                java.math.BigDecimal threshold,
        @Schema(description = "满减金额，元，仅FIXED填写")
                @DecimalMin("0.01")
                @Digits(integer = 17, fraction = 2)
                java.math.BigDecimal fixedAmount,
        @Schema(description = "支付百分比，80表示8折，仅PERCENT填写") @Min(1) @Max(99) Integer discountPercent,
        @Schema(description = "折扣金额上限，仅PERCENT可填写")
                @DecimalMin("0.01")
                @Digits(integer = 17, fraction = 2)
                java.math.BigDecimal discountLimit,
        @Schema(description = "总发放数量，每人每模板限一张") @NotNull @Min(1) @Max(1000000) Integer totalCount,
        @Schema(description = "数据库同一时区，yyyy-MM-dd HH:mm:ss") @NotNull
                java.time.LocalDateTime validFrom,
        @Schema(description = "有效期右开边界，yyyy-MM-dd HH:mm:ss") @NotNull
                java.time.LocalDateTime validUntil)
        implements Serializable {}
