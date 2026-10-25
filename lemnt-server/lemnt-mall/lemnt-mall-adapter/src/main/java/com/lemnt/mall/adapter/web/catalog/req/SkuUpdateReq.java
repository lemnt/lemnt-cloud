package com.lemnt.mall.adapter.web.catalog.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Schema(description = "SkuUpdateReq")
public record SkuUpdateReq(
        @Schema(description = "SKU名称") @NotBlank @Size(max = 200) String name,
        @Schema(description = "销售价，元")
                @NotNull
                @DecimalMin("0.01")
                @Digits(integer = 17, fraction = 2)
                java.math.BigDecimal price,
        @Schema(description = "SKU图片URL") @Size(max = 500) String image)
        implements Serializable {}
