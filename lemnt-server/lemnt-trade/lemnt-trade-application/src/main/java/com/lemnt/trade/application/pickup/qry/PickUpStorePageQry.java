package com.lemnt.trade.application.pickup.qry;

import java.io.Serializable;

/** 自提门店分页查询。 */
public record PickUpStorePageQry(String name, Integer status, int pageNum, int pageSize)
        implements Serializable {
}
