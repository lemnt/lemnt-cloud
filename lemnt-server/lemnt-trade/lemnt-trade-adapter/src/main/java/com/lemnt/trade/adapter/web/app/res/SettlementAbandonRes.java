package com.lemnt.trade.adapter.web.app.res;

import io.swagger.v3.oas.annotations.media.Schema;
import com.lemnt.trade.adapter.web.checkout.res.CheckoutReceiptRes;

@Schema(description = "终止未完成提单的结果，不关闭已生成订单")
public record SettlementAbandonRes(
        @Schema(description = "是否已持久化终止标记，阻止后续延迟提单") boolean abandoned,
        @Schema(description = "已生成时返回原订单，此时abandoned为false") CheckoutReceiptRes order)
        implements java.io.Serializable {}
