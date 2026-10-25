package com.lemnt.trade.adapter.web.fulfillment.res;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "订单履约OrderPageRes")
public record OrderPageRes(
        @Schema(description = "分页记录") java.util.List<OrderSummaryRes> records,
        @Schema(description = "总记录数") long total)
        implements Serializable {}
