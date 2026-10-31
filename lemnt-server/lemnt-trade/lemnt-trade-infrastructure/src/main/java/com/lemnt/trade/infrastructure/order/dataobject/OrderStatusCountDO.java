package com.lemnt.trade.infrastructure.order.dataobject;

import lombok.Getter;
import lombok.Setter;

/** 订单状态分组计数投影。 */
@Getter
@Setter
public class OrderStatusCountDO {
    private String status;
    private Boolean commentStatus;
    private long cnt;
}
