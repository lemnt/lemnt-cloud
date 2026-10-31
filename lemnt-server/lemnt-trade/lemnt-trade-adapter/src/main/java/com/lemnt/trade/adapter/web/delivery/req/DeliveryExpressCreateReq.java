package com.lemnt.trade.adapter.web.delivery.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 新增快递公司。 */
@Schema(description = "新增快递公司")
public record DeliveryExpressCreateReq(
        @Schema(description = "快递公司编码")
        @NotBlank @Size(max = 40)
        String code,
        @Schema(description = "快递公司名称")
        @NotBlank @Size(max = 100)
        String name,
        @Schema(description = "图标地址")
        @Size(max = 500)
        String logo,
        @Schema(description = "排序")
        @NotNull
        @DecimalMin("0")
        Integer sort,
        @Schema(description = "状态，0 启用、1 停用")
        @NotNull
        @Min(0) @Max(1)
        Integer status
) implements Serializable {
}
