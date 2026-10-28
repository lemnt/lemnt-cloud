package com.lemnt.trade.adapter.web.delivery.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/** 修改运费模板。 */
@Schema(description = "修改运费模板")
public record DeliveryExpressTemplateUpdateReq(
        @Schema(description = "模板名称")
        @NotBlank @Size(max = 100)
        String name,
        @Schema(description = "计费方式，1 件数、2 重量千克、3 体积立方米")
        @NotNull
        @Min(1) @Max(3)
        Integer chargeMode,
        @Schema(description = "排序")
        @NotNull
        @DecimalMin("0")
        Integer sort,
        @Schema(description = "计费规则")
        @NotNull @Size(max = 100)
        @Valid
        List<DeliveryChargeReq> charges,
        @Schema(description = "包邮规则")
        @NotNull @Size(max = 100)
        @Valid
        List<DeliveryFreeReq> frees
) implements Serializable {
}
