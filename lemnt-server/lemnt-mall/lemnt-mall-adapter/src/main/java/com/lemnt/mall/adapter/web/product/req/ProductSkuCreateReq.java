package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 商品SKU创建请求。
 */
@Schema(description = "ProductSkuCreateReq")
public record ProductSkuCreateReq(
        @Schema(description = "SKU编码")
        @NotBlank
        @Size(max = 64)
        String code,
        @Schema(description = "SKU名称")
        @NotBlank
        @Size(max = 200)
        String name,
        @Schema(description = "销售价，元，最多两位小数")
        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal price,
        @Schema(description = "可用库存")
        @NotNull
        @Min(0)
        Integer stock,
        @Schema(description = "SKU图片URL")
        @Size(max = 500)
        String image,
        @Schema(description = "规格名到规格值的映射，最多10项")
        @Size(max = 10)
        Map<@NotBlank
        @Size(max = 64)
        String,
        @NotBlank
        @Size(max = 100)
        String>
        properties,
        @Schema(description = "重量，千克") @DecimalMin("0") BigDecimal weight,
        @Schema(description = "体积，立方米") @DecimalMin("0") BigDecimal volume,
        @Schema(description = "规格编号到规格值编号，名称由服务端字典读取")
        @Size(max = 10)
        Map<@jakarta.validation.constraints.Positive Long, @NotNull @jakarta.validation.constraints.Positive Long> specValueIds
) implements Serializable {}
