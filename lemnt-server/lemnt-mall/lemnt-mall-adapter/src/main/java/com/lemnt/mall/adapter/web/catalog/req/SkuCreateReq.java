package com.lemnt.mall.adapter.web.catalog.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;

@Schema(description = "SkuCreateReq")
public record SkuCreateReq(
        @Schema(description = "SKU编码") @NotBlank @Size(max = 64) String code,
        @Schema(description = "SKU名称") @NotBlank @Size(max = 200) String name,
        @Schema(description = "销售价，元，最多两位小数")
                @NotNull
                @DecimalMin("0.01")
                @Digits(integer = 17, fraction = 2)
                java.math.BigDecimal price,
        @Schema(description = "可用库存") @NotNull @Min(0) Integer stock,
        @Schema(description = "SKU图片URL") @Size(max = 500) String image,
        @Schema(description = "规格名到规格值的映射，最多10项") @Size(max = 10)
                java.util.Map<@NotBlank @Size(max = 64) String, @NotBlank @Size(max = 100) String>
                        properties)
        implements Serializable {}
