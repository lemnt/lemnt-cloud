package com.lemnt.trade.application.delivery.qry;

import java.util.List;

/** 快递计价输入，金额及商品信息必须来自服务端。 */
public record DeliveryPriceQry(
        Integer areaId,
        Boolean freeDelivery,
        List<DeliveryPriceItemQry> items
) {
}
