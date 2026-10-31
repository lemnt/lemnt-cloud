package com.lemnt.trade.client.bo;

import java.io.Serializable;
import java.math.BigDecimal;

/** 可信服务端商品快照，金额分。 */
public record DeliveryFeeItemBo(
        Long skuId,
        Long templateId,
        Integer count,
        BigDecimal weight,
        BigDecimal volume,
        Integer payPrice
) implements Serializable {
}
