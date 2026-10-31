package com.lemnt.mall.domain.settlement.gateway;

import com.lemnt.mall.domain.settlement.model.DeliveryPriceLine;
import com.lemnt.mall.domain.settlement.model.DeliveryPriceResult;

import java.util.List;

/** 使用服务端商品快照查询运费，金额统一为元。 */
public interface DeliveryPriceGateway {
    /** 读取当前区域运费，出错时不能当作零运费。 */
    DeliveryPriceResult price(Integer areaId, List<DeliveryPriceLine> items);
}
