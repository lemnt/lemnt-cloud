package com.lemnt.trade.adapter.web.aftersale.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** 退货运单登记请求。 */
@Schema(description = "退货运单登记请求")
public record AfterSaleReturnReq(
        @Schema(description = "退货承运商编码")
        @NotBlank @Size(max = 40)
        String carrierCode,
        @Schema(description = "退货运单编号")
        @NotBlank @Size(max = 100)
        String trackingNo
) implements Serializable {
}
