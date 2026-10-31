package com.lemnt.mall.adapter.web.product.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 规格字典修改请求。 */
@Schema(description = "规格字典修改请求")
public record ProductSpecValueUpdateReq(
        @Schema(description = "规格值名称")
        @NotBlank
        @Size(max = 100)
        String name,
        @Schema(description = "说明，可空")
        @Size(max = 500)
        String remark
) implements Serializable {
}
