package com.lemnt.trade.application.delivery.cmd;

/** 新增快递公司命令。 */
public record DeliveryExpressCreateCmd(
        String code,
        String name,
        String logo,
        Integer sort,
        Integer status
) implements java.io.Serializable {
}
