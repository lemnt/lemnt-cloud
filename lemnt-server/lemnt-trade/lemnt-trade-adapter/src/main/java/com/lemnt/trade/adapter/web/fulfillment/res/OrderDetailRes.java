package com.lemnt.trade.adapter.web.fulfillment.res;

import java.io.Serializable;
import io.swagger.v3.oas.annotations.media.Schema;
import com.lemnt.trade.adapter.web.checkout.res.CheckoutItemRes;

@Schema(description = "订单履约OrderDetailRes")
public record OrderDetailRes(
        @Schema(description = "订单编号") String orderNo,
        @Schema(description = "会员ID") Long memberId,
        @Schema(description = "应付金额，人民币元") java.math.BigDecimal totalAmount,
        @Schema(description = "订单状态：RESERVING/PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CLOSED")
                String status,
        @Schema(description = "全部订单库存是否已确认") boolean inventoryConfirmed,
        @Schema(description = "是否有待处理退款申请") boolean activeRefund,
        @Schema(description = "订单商品明细") java.util.List<CheckoutItemRes> items,
        @Schema(description = "物流记录，包含收件人信息，仅详情权限可见") ShipmentRes shipment)
        implements Serializable {}
