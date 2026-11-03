package com.lemnt.order.adapter.web.order.converter;

import com.lemnt.order.adapter.web.order.req.OrderRemarkUpdateReq;
import com.lemnt.order.application.order.cmd.OrderRemarkUpdateCmd;

/** 订单管理请求转换。 */
public final class OrderManagementReqConverter {
    private OrderManagementReqConverter() {}

    /** 转换备注修改命令。 */
    public static OrderRemarkUpdateCmd remark(String no, OrderRemarkUpdateReq req) {
        return new OrderRemarkUpdateCmd(no, req.remark());
    }
}
