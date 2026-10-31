package com.lemnt.trade.adapter.web.order.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/** 订单备注响应。 */
@Schema(description = "订单备注响应")
public record OrderRemarkRes(
        @Schema(description = "后台备注，未填写为空字符串")
        String remark
) implements Serializable {
}
