package com.lemnt.trade.domain.payment.model;

import java.io.Serializable;

/** 派发任务分页筛选。 */
public record PayNotifyTaskFilter(
        String type,
        String bizNo,
        String orderNo,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}
