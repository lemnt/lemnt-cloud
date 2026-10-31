package com.lemnt.trade.adapter.web.shipment.converter;

import com.lemnt.trade.adapter.web.shipment.req.ShipmentPageReq;
import com.lemnt.trade.application.fulfillment.qry.ShipmentPageQry;

/** 物流查询请求转换。 */
public final class ShipmentReqConverter {
    private ShipmentReqConverter() {
    }

    /** 转换分页默认值。 */
    public static ShipmentPageQry page(ShipmentPageReq req) {
        return new ShipmentPageQry(req.orderNo(), req.carrierCode(), req.trackingNo(), req.status(),
                req.pageNum() == null ? 1 : req.pageNum(), req.pageSize() == null ? 20 : req.pageSize());
    }
}
