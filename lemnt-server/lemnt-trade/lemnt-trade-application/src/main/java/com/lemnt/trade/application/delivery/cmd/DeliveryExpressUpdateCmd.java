package com.lemnt.trade.application.delivery.cmd;

/** 修改快递公司命令。 */
public record DeliveryExpressUpdateCmd(
        Long id,
        String code,
        String name,
        String logo,
        Integer sort,
        Integer status
) implements java.io.Serializable {
}
