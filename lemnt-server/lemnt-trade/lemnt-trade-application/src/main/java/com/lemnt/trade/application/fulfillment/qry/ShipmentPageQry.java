package com.lemnt.trade.application.fulfillment.qry;

import java.io.Serializable;

/** 物流单分页查询。 */
public record ShipmentPageQry(
        String orderNo,
        String carrierCode,
        String trackingNo,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}
