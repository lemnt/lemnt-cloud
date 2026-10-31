package com.lemnt.trade.infrastructure.payment.converter;

import com.lemnt.trade.domain.payment.model.PayApp;
import com.lemnt.trade.infrastructure.payment.dataobject.PayAppDO;

/** 支付应用与表字段转换。 */
public final class PayAppConverter {
    private PayAppConverter() {
    }

    /** 读取业务对象。 */
    public static PayApp toEntity(PayAppDO row) {
        if (row == null) {
            return null;
        }
        return new PayApp(row.getId(), row.getAppKey(), row.getName(), row.getStatus(), row.getRemark(),
                row.getOrderNotifyUrl(), row.getRefundNotifyUrl(), row.getCreateTime());
    }

    /** 写入表字段。 */
    public static PayAppDO toDO(PayApp value) {
        var row = new PayAppDO();
        row.setId(value.id());
        row.setAppKey(value.appKey());
        row.setName(value.name());
        row.setStatus(value.status());
        row.setRemark(value.remark());
        row.setOrderNotifyUrl(value.orderNotifyUrl());
        row.setRefundNotifyUrl(value.refundNotifyUrl());
        return row;
    }
}
