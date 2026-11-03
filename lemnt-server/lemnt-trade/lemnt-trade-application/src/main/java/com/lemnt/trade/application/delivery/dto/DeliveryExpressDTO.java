package com.lemnt.trade.application.delivery.dto;

import java.io.Serializable;

/** 配送主体传输对象。 */
public record DeliveryExpressDTO(
        Long id, String code, String name, String logo, Integer sort, Integer status)
        implements Serializable {}
