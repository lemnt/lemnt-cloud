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

/** 下架商品追加SKU请求。 */
@Schema(description = "下架商品追加SKU请求")
public record ProductSkuAddReq(
        @Schema(description = "全局唯一SKU编码")
        @NotBlank
        @Size(max = 64)
        String code,
        @Schema(description = "SKU名称")
        @NotBlank
        @Size(max = 200)
        String name,
        @Schema(description = "单价，单位元")
        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 17, fraction = 2)
        BigDecimal price,
        @Schema(description = "初始可用库存")
        @NotNull
        @Min(0)
        Integer stock,
        @Schema(description = "图片地址，可空")
        @Size(max = 500)
        String image,
        @Schema(description = "最多10项规格")
        @Size(max = 10)
        Map<String, String> properties
) implements Serializable {
}
