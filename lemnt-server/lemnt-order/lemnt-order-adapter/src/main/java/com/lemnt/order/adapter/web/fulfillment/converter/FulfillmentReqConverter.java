package com.lemnt.order.adapter.web.fulfillment.converter;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.order.adapter.web.fulfillment.req.OrderPageReq;
import com.lemnt.order.adapter.web.fulfillment.req.RefundPageReq;
import com.lemnt.order.adapter.web.fulfillment.req.RefundRejectReq;
import com.lemnt.order.adapter.web.fulfillment.req.RefundRequestReq;
import com.lemnt.order.adapter.web.fulfillment.req.ShipmentCreateReq;
import com.lemnt.order.application.fulfillment.cmd.RefundRejectCmd;
import com.lemnt.order.application.fulfillment.cmd.RefundRequestCmd;
import com.lemnt.order.application.fulfillment.cmd.ShipmentCreateCmd;
import com.lemnt.order.application.fulfillment.qry.OrderPageQry;
import com.lemnt.order.application.fulfillment.qry.RefundPageQry;
import com.lemnt.order.domain.order.model.TradeOperator;

/**
 * 履约请求转换器。
 */
public final class FulfillmentReqConverter {
    private FulfillmentReqConverter() {}

    /**
     * 发货。
     */
    public static ShipmentCreateCmd shipment(String no, ShipmentCreateReq r) {
        return new ShipmentCreateCmd(
                no,
                r.requestNo(),
                r.carrierCode(),
                r.trackingNo(),
                r.recipientName(),
                r.phone(),
                r.address(),
                TradeOperator.administrator(SecurityUtils.getUserId()));
    }

    /**
     * 退款。
     */
    public static RefundRequestCmd refund(String no, RefundRequestReq r) {
        return new RefundRequestCmd(no, r.requestNo(), r.reason());
    }

    /**
     * 拒绝。
     */
    public static RefundRejectCmd reject(String no, RefundRejectReq r) {
        return new RefundRejectCmd(no, r.reason());
    }

    /**
     * 查询订单。
     */
    public static OrderPageQry orders(OrderPageReq r) {
        return new OrderPageQry(
                r.orderNo(),
                r.status(),
                r.pageNum() == null ? 1 : r.pageNum(),
                r.pageSize() == null ? 20 : r.pageSize());
    }

    /**
     * 查询退款。
     */
    public static RefundPageQry refunds(RefundPageReq r) {
        return new RefundPageQry(
                r.orderNo(),
                r.status(),
                r.pageNum() == null ? 1 : r.pageNum(),
                r.pageSize() == null ? 20 : r.pageSize());
    }
}
