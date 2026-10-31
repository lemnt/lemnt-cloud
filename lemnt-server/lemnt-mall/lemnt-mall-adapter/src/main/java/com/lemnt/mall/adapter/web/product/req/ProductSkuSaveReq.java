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
import java.util.List;

/**
 * 商品SKU创建请求。
 */
@Schema(description = "ProductSkuSaveReq")
public record ProductSkuSaveReq(
        @Schema(description = "已有SKU编号，新增时为空")
        Long id,
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
        @Schema(description = "规格对象列表，最多10项")
        @Size(max = 10)
        @NotNull
        List<@NotNull @jakarta.validation.Valid Property> properties,
        @Schema(description = "重量，千克") @DecimalMin("0") BigDecimal weight,
        @Schema(description = "体积，立方米") @DecimalMin("0") BigDecimal volume
) implements Serializable {
    /** 编号与名称保持一一对应。 */
    @Schema(description = "商品规格")
    public record Property(
            @Schema(description = "规格项编号")
            @NotNull @jakarta.validation.constraints.Positive
            Long propertyId,
            @Schema(description = "规格名称，写入时可省略")
            @Size(max = 64)
            String propertyName,
            @Schema(description = "规格值编号")
            @NotNull @jakarta.validation.constraints.Positive
            Long valueId,
            @Schema(description = "规格值名称，写入时可省略")
            @Size(max = 100)
            String valueName
    ) implements java.io.Serializable {}
}
