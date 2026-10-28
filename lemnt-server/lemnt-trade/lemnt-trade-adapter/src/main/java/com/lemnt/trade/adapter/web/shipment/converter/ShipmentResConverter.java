package com.lemnt.trade.adapter.web.shipment.converter;

import com.lemnt.trade.adapter.web.shipment.res.ShipmentDetailRes;
import com.lemnt.trade.adapter.web.shipment.res.ShipmentPageRes;
import com.lemnt.trade.adapter.web.shipment.res.ShipmentTrackRes;
import com.lemnt.trade.application.fulfillment.dto.ShipmentDTO;
import com.lemnt.trade.application.fulfillment.dto.ShipmentSummaryDTO;
import com.lemnt.trade.application.fulfillment.dto.ShipmentTrackDTO;

/** 物流接口响应转换。 */
public final class ShipmentResConverter {
    private ShipmentResConverter() {
    }

    /** 列表不暴露收件信息。 */
    public static ShipmentPageRes page(ShipmentSummaryDTO value) {
        return new ShipmentPageRes(value.orderNo(), value.carrierCode(), value.trackingNo(),
                value.shippedAt(), value.receivedAt());
    }

    /** 单独授权的物流详情。 */
    public static ShipmentDetailRes detail(ShipmentDTO value) {
        return new ShipmentDetailRes(value.orderNo(), value.carrierCode(), value.trackingNo(),
                value.shippedAt(), value.receivedAt(), value.recipientName(), value.phone(), value.address());
    }

    /** 真实渠道轨迹转换。 */
    public static ShipmentTrackRes track(ShipmentTrackDTO value) {
        return new ShipmentTrackRes(value.time(), value.description());
    }
}
