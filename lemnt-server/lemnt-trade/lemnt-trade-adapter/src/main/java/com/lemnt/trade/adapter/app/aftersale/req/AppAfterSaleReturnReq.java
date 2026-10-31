package com.lemnt.trade.adapter.app.aftersale.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** 会员登记退货运单请求。 */
@Schema(description = "会员登记退货运单请求")
public record AppAfterSaleReturnReq(
        @Schema(description = "退货承运商")
        @NotBlank
        @Size(max = 40)
        String carrierCode,
        @Schema(description = "退货运单编号")
        @NotBlank
        @Size(max = 100)
        String trackingNo
) implements Serializable {
}
