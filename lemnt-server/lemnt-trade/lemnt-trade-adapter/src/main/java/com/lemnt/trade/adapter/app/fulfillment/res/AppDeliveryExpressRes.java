package com.lemnt.trade.adapter.app.fulfillment.res;

import io.swagger.v3.oas.annotations.media.Schema;

/** 小程序快递公司。 */
@Schema(description = "小程序快递公司")
public record AppDeliveryExpressRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "编码") String code,
        @Schema(description = "名称") String name)
        implements java.io.Serializable {}
