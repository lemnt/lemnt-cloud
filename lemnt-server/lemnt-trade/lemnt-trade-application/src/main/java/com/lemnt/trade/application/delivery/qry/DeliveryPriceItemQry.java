package com.lemnt.trade.application.delivery.qry;

import java.math.BigDecimal;

/** 服务端商品快照，重量千克、体积立方米、金额分。 */
public record DeliveryPriceItemQry(
        Long skuId,
        Long templateId,
        Integer count,
        BigDecimal weight,
        BigDecimal volume,
        Integer payPrice
) {
}
