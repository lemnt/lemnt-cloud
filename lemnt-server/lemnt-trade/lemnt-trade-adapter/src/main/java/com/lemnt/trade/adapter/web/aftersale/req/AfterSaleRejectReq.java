package com.lemnt.trade.adapter.web.aftersale.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/** 商品售后拒绝请求。 */
@Schema(description = "商品售后拒绝请求")
public record AfterSaleRejectReq(
        @Schema(description = "处理原因")
        @NotBlank
        @Size(max = 500)
        String reason
) implements Serializable {
}
