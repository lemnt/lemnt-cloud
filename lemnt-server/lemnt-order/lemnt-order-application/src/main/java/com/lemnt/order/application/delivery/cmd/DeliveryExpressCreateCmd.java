package com.lemnt.order.application.delivery.cmd;

import java.io.Serializable;

/** 新增快递公司命令。 */
public record DeliveryExpressCreateCmd(
        String code, String name, String logo, Integer sort, Integer status)
        implements Serializable {}
