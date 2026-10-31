package com.lemnt.trade.application.delivery.cmd;

import java.util.List;

/** 运费包邮规则输入。 */
public record DeliveryFreeCmd(
        Long id,
        List<Integer> areaIds,
        Integer freePrice,
        Integer freeCount
) implements java.io.Serializable {
}
