package com.lemnt.trade.application.payment.qry;

import java.io.Serializable;

/** 派发任务分页查询。 */
public record PayNotifyTaskPageQry(
        String type,
        String bizNo,
        String orderNo,
        String status,
        int pageNum,
        int pageSize
) implements Serializable {
}
