package com.lemnt.trade.application.delivery.cmd;

import java.util.List;

/** 新增运费模板命令。 */
public record DeliveryExpressTemplateCreateCmd(
        String name,
        Integer chargeMode,
        Integer sort,
        List<DeliveryChargeCmd> charges,
        List<DeliveryFreeCmd> frees
) implements java.io.Serializable {
}
