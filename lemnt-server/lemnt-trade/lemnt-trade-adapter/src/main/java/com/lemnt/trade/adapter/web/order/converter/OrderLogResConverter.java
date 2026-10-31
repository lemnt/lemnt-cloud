package com.lemnt.trade.adapter.web.order.converter;

import com.lemnt.trade.adapter.web.order.res.OrderLogRes;
import com.lemnt.trade.application.order.dto.OrderLogDTO;

/** 当前订单日志接口转换。 */
public final class OrderLogResConverter {
    private OrderLogResConverter() {}
    /** 转换后台日志响应。 */
    public static OrderLogRes response(OrderLogDTO value) {
        return new OrderLogRes(value.id(), value.userId(), value.userType(), value.orderId(), value.beforeStatus(),
                value.afterStatus(), value.operateType(), value.content(), value.createTime());
    }
}
