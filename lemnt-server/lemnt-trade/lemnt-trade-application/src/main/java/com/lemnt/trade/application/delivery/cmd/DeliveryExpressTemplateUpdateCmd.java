package com.lemnt.trade.application.delivery.cmd;

import java.util.List;

/** 修改运费模板命令。 */
public record DeliveryExpressTemplateUpdateCmd(
        Long id,
        String name,
        Integer chargeMode,
        Integer sort,
        List<DeliveryChargeCmd> charges,
        List<DeliveryFreeCmd> frees
) implements java.io.Serializable {
}
