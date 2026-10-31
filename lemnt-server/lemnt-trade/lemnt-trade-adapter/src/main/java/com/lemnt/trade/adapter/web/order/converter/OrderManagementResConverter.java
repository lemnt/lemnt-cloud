package com.lemnt.trade.adapter.web.order.converter;

import com.lemnt.trade.adapter.web.order.res.OrderRemarkRes;
import com.lemnt.trade.adapter.web.order.res.OrderStatisticsRes;
import com.lemnt.trade.application.order.dto.OrderRemarkDTO;
import com.lemnt.trade.application.order.dto.OrderStatisticsDTO;

/** 订单管理响应转换。 */
public final class OrderManagementResConverter {
    private OrderManagementResConverter() {
    }

    /** 统计精度沿用统一金额序列化。 */
    public static OrderStatisticsRes statistics(OrderStatisticsDTO value) {
        return new OrderStatisticsRes(value.orderCount(), value.orderPayPrice().setScale(2),
                value.afterSaleCount(), value.afterSalePrice().setScale(2));
    }

    /** 转换备注响应。 */
    public static OrderRemarkRes remark(OrderRemarkDTO value) {
        return new OrderRemarkRes(value.remark());
    }
}
