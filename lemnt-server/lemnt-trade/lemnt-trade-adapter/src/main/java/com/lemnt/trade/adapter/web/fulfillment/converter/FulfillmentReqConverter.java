package com.lemnt.trade.adapter.web.fulfillment.converter;

import com.lemnt.trade.adapter.web.fulfillment.req.*;
import com.lemnt.trade.application.fulfillment.cmd.*;
import com.lemnt.trade.application.fulfillment.qry.*;

public final class FulfillmentReqConverter {
    private FulfillmentReqConverter() {}

    public static ShipmentCreateCmd shipment(String no, ShipmentCreateReq r) {
        return new ShipmentCreateCmd(
                no,
                r.requestNo(),
                r.carrierCode(),
                r.trackingNo(),
                r.recipientName(),
                r.phone(),
                r.address());
    }

    public static RefundRequestCmd refund(String no, RefundRequestReq r) {
        return new RefundRequestCmd(no, r.requestNo(), r.reason());
    }

    public static RefundRejectCmd reject(String no, RefundRejectReq r) {
        return new RefundRejectCmd(no, r.reason());
    }

    public static OrderPageQry orders(OrderPageReq r) {
        return new OrderPageQry(
                r.orderNo(),
                r.status(),
                r.pageNum() == null ? 1 : r.pageNum(),
                r.pageSize() == null ? 20 : r.pageSize());
    }

    public static RefundPageQry refunds(RefundPageReq r) {
        return new RefundPageQry(
                r.orderNo(),
                r.status(),
                r.pageNum() == null ? 1 : r.pageNum(),
                r.pageSize() == null ? 20 : r.pageSize());
    }
}
