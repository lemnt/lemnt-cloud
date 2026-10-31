package com.lemnt.trade.client.dto;

import java.io.Serializable;
import java.util.Map;

/** 运费及商品行运费，整数分。 */
public record DeliveryFeeDTO(
        Integer deliveryPrice,
        Map<Long, Integer> itemPrices
) implements Serializable {
}
