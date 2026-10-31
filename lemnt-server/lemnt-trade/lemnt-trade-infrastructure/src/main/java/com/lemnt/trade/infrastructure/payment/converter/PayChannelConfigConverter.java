package com.lemnt.trade.infrastructure.payment.converter;

import com.lemnt.trade.domain.payment.model.PayChannelConfig;
import com.lemnt.trade.infrastructure.payment.dataobject.PayChannelConfigDO;

/** 支付渠道配置与表字段转换。 */
public final class PayChannelConfigConverter {
    private PayChannelConfigConverter() {
    }

    /** 读取业务对象。 */
    public static PayChannelConfig toEntity(PayChannelConfigDO row) {
        if (row == null) {
            return null;
        }
        return new PayChannelConfig(row.getId(), row.getAppId(), row.getCode(), row.getStatus(),
                row.getFeeRate(), row.getRemark(), row.getConfig(), row.getCreateTime());
    }

    /** 写入表字段。 */
    public static PayChannelConfigDO toDO(PayChannelConfig value) {
        var row = new PayChannelConfigDO();
        row.setId(value.id());
        row.setAppId(value.appId());
        row.setCode(value.code());
        row.setStatus(value.status());
        row.setFeeRate(value.feeRate());
        row.setRemark(value.remark());
        row.setConfig(value.config());
        return row;
    }
}
