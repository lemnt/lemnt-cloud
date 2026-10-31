package com.lemnt.trade.infrastructure.order.converter;

import com.lemnt.trade.domain.order.model.OrderLog;
import com.lemnt.trade.infrastructure.order.dataobject.OrderLogDO;

/** 订单日志持久化转换。 */
public final class OrderLogConverter {
    private OrderLogConverter() {}
    /** 转换日志写入行。 */
    public static OrderLogDO toDO(OrderLog value) {
        var row = new OrderLogDO();
        row.setUserId(value.userId());
        row.setUserType(value.userType());
        row.setOrderId(value.orderId());
        row.setBeforeStatus(value.beforeStatus());
        row.setAfterStatus(value.afterStatus());
        row.setOperateType(value.operateType());
        row.setContent(value.content());
        row.setCreateTime(value.createTime());
        return row;
    }
    /** 转换查询结果。 */
    public static OrderLog toModel(OrderLogDO row) {
        return new OrderLog(row.getId(), row.getUserId(), row.getUserType(), row.getOrderId(), row.getBeforeStatus(),
                row.getAfterStatus(), row.getOperateType(), row.getContent(), row.getCreateTime());
    }
}
