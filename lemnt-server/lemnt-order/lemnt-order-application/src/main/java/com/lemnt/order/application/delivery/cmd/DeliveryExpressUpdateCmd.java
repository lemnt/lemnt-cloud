package com.lemnt.order.application.delivery.cmd;

import java.io.Serializable;

/** 修改快递公司命令。 */
public record DeliveryExpressUpdateCmd(
        Long id, String code, String name, String logo, Integer sort, Integer status)
        implements Serializable {}
