package com.lemnt.trade.domain.delivery.model;

import java.io.Serializable;

/** 配送管理分页条件。 */
public record DeliveryExpressFilter(String name, Integer status, Integer pageNum, Integer pageSize)
        implements Serializable {}
