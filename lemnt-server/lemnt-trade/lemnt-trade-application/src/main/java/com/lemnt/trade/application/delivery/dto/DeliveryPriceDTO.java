package com.lemnt.trade.application.delivery.dto;

import java.util.Map;

/** 运费及逐 SKU 运费分摊，单位分。 */
public record DeliveryPriceDTO(
        Integer deliveryPrice,
        Map<Long, Integer> itemPrices
) {
}
