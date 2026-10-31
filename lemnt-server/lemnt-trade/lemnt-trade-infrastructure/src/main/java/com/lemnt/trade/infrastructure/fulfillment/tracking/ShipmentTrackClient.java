package com.lemnt.trade.infrastructure.fulfillment.tracking;

import com.lemnt.trade.domain.fulfillment.model.Shipment;
import com.lemnt.trade.domain.fulfillment.model.ShipmentTrack;

import java.util.List;

/** 承运商轨迹适配器，负责鉴权、签名、超时和厂商响应转换。 */
public interface ShipmentTrackClient {
    /** 返回支持的承运商编码。 */
    String carrierCode();

    /** 查询真实轨迹，空列表表示查询成功但暂无轨迹，失败必须抛出异常。 */
    List<ShipmentTrack> tracks(Shipment shipment);
}
