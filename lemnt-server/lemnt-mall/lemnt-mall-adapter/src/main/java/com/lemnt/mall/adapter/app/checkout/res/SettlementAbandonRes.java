package com.lemnt.mall.adapter.app.checkout.res;

import com.lemnt.mall.adapter.app.checkout.res.CheckoutReceiptRes;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

/**
 * 终止未完成提单的结果，不关闭已生成订单。
 */
@Schema(description = "终止未完成提单的结果，不关闭已生成订单")
public record SettlementAbandonRes(
        @Schema(description = "是否已持久化终止标记，阻止后续延迟提单")
        boolean abandoned,
        @Schema(description = "已生成时返回原订单，此时abandoned为false")
        CheckoutReceiptRes order
) implements Serializable {}
