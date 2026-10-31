package com.lemnt.trade.adapter.app.order.converter;

import com.lemnt.trade.adapter.app.order.res.AppOrderDetailRes;
import com.lemnt.trade.adapter.app.order.res.AppOrderItemRes;
import com.lemnt.trade.adapter.app.order.res.AppOrderShipmentRes;
import com.lemnt.trade.adapter.app.order.res.AppOrderStatusCountRes;
import com.lemnt.trade.adapter.app.order.res.AppOrderSummaryRes;
import com.lemnt.trade.adapter.app.order.res.AppOrderTrackRes;
import com.lemnt.trade.application.fulfillment.dto.MemberOrderDetailDTO;
import com.lemnt.trade.application.fulfillment.dto.MemberOrderStatusCountDTO;
import com.lemnt.trade.application.fulfillment.dto.MemberOrderSummaryDTO;
import com.lemnt.trade.application.fulfillment.dto.OrderItemDTO;
import com.lemnt.trade.application.fulfillment.dto.ShipmentDTO;
import com.lemnt.trade.application.fulfillment.dto.ShipmentTrackDTO;

/** 会员订单接口响应转换。 */
public final class AppOrderResConverter {
    private AppOrderResConverter() {
    }

    /** 转换订单列表项。 */
    public static AppOrderSummaryRes summary(MemberOrderSummaryDTO value) {
        return new AppOrderSummaryRes(value.orderNo(), value.status(), value.totalAmount(), value.createTime(),
                value.commentStatus(), value.finishTime(), value.activeRefund(),
                value.items().stream().map(AppOrderResConverter::item).toList());
    }

    /** 转换订单详情。 */
    public static AppOrderDetailRes detail(MemberOrderDetailDTO value) {
        return new AppOrderDetailRes(value.orderNo(), value.status(), value.totalAmount(), value.createTime(),
                value.activeRefund(), value.items().stream().map(AppOrderResConverter::item).toList(),
                shipment(value.shipment()), value.commentStatus(), value.finishTime());
    }

    /** 转换状态数量。 */
    public static AppOrderStatusCountRes statusCount(MemberOrderStatusCountDTO value) {
        return new AppOrderStatusCountRes(value.pendingPayment(), value.awaitingShipment(), value.awaitingReceipt(),
                value.awaitingComment(), value.completed(), value.closed(), value.refunded());
    }

    /** 转换物流轨迹。 */
    public static AppOrderTrackRes track(ShipmentTrackDTO value) {
        return new AppOrderTrackRes(value.time(), value.description());
    }

    private static AppOrderShipmentRes shipment(ShipmentDTO value) {
        return value == null
                ? null
                : new AppOrderShipmentRes(value.carrierCode(), value.trackingNo(), value.recipientName(),
                        value.phone(), value.address(), value.shippedAt(), value.receivedAt());
    }

    private static AppOrderItemRes item(OrderItemDTO value) {
        return new AppOrderItemRes(value.productId(), value.productCode(), value.productName(), value.unitPrice(),
                value.quantity(), value.lineAmount(), value.spuId(), value.spuName(),
                value.properties() == null ? null : value.properties().stream()
                        .map(property -> new AppOrderItemRes.Property(property.propertyId(), property.propertyName(),
                                property.valueId(), property.valueName())).toList(),
                value.picUrl(), value.orderItemId(), value.commentStatus());
    }
}
