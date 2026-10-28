package com.lemnt.trade.application.delivery.cmd;

import java.math.BigDecimal;
import java.util.List;

/** 运费计费规则输入。 */
public record DeliveryChargeCmd(
        Long id,
        List<Integer> areaIds,
        BigDecimal startCount,
        Integer startPrice,
        BigDecimal extraCount,
        Integer extraPrice
) implements java.io.Serializable {
}
