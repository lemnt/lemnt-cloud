package com.lemnt.trade.adapter.web.order.req;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

/** 订单备注修改请求。 */
@Schema(description = "订单备注修改请求")
public record OrderRemarkUpdateReq(
        @Schema(description = "后台备注，空字符串清除")
        @NotNull @Size(max = 500)
        String remark
) implements Serializable {
}
