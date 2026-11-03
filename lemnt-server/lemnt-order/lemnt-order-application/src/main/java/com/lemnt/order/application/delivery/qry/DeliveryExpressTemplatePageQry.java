package com.lemnt.order.application.delivery.qry;

import java.io.Serializable;

/** 配送管理分页查询。 */
public record DeliveryExpressTemplatePageQry(String name, Integer pageNum, Integer pageSize)
        implements Serializable {}
