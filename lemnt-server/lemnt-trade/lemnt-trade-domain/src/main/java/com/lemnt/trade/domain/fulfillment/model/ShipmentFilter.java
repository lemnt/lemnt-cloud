package com.lemnt.trade.domain.fulfillment.model;

import java.io.Serializable;

/** 物流单查询条件。 */
public record ShipmentFilter(
        String orderNo,
        String carrierCode,
        String trackingNo,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}
