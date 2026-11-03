package com.lemnt.order.domain.delivery.model;

import java.io.Serializable;

/** 配送管理分页条件。 */
public record DeliveryExpressTemplateFilter(String name, Integer pageNum, Integer pageSize)
        implements Serializable {}
