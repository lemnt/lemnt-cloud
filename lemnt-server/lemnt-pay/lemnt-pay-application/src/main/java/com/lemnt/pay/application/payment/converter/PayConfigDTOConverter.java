package com.lemnt.pay.application.payment.converter;

import com.lemnt.pay.application.payment.dto.PayAppDTO;
import com.lemnt.pay.application.payment.dto.PayChannelConfigDTO;
import com.lemnt.pay.domain.payment.model.PayApp;
import com.lemnt.pay.domain.payment.model.PayChannelConfig;

/**
 * 支付应用与渠道配置应用输出转换。
 */
public final class PayConfigDTOConverter {
    private PayConfigDTOConverter() {
    }

    public static PayAppDTO payApp(PayApp value) {
        return value == null
            ? null
            : new PayAppDTO(
            value.id(),
            value.appKey(),
            value.name(),
            value.status(),
            value.remark(),
            value.orderNotifyUrl(),
            value.refundNotifyUrl(),
            value.createTime());
    }

    public static PayChannelConfigDTO payChannel(PayChannelConfig value) {
        return value == null
            ? null
            : new PayChannelConfigDTO(
            value.id(),
            value.appId(),
            value.code(),
            value.status(),
            value.feeRate(),
            value.remark(),
            value.config(),
            value.createTime());
    }
}
