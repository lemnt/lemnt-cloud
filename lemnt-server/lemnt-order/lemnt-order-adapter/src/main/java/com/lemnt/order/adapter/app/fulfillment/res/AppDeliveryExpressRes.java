package com.lemnt.order.adapter.app.fulfillment.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 小程序快递公司。 */
@Schema(description = "小程序快递公司")
public record AppDeliveryExpressRes(
        @Schema(description = "编号") Long id,
        @Schema(description = "编码") String code,
        @Schema(description = "名称") String name)
        implements Serializable {}
