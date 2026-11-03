package com.lemnt.order.application.delivery.cmd;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 运费计费规则输入。 */
public record DeliveryChargeCmd(
        Long id,
        List<Integer> areaIds,
        BigDecimal startCount,
        Integer startPrice,
        BigDecimal extraCount,
        Integer extraPrice)
        implements Serializable {}
