package com.lemnt.trade.adapter.web.payapp.converter;

import com.lemnt.trade.adapter.web.payapp.res.PayAppRes;
import com.lemnt.trade.adapter.web.paychannel.res.PayChannelConfigRes;
import com.lemnt.trade.application.payment.dto.PayAppDTO;
import com.lemnt.trade.application.payment.dto.PayChannelConfigDTO;

/** 支付应用与渠道配置响应转换。 */
public final class PayConfigResConverter {
    private PayConfigResConverter() {
    }

    public static PayAppRes payApp(PayAppDTO value) {
        return value == null
                ? null
                : new PayAppRes(value.id(), value.appKey(), value.name(), value.status(), value.remark(),
                        value.orderNotifyUrl(), value.refundNotifyUrl(), value.createTime());
    }

    /** 渠道配置不返回明文，只返回是否已配置。 */
    public static PayChannelConfigRes payChannel(PayChannelConfigDTO value) {
        return value == null
                ? null
                : new PayChannelConfigRes(value.id(), value.appId(), value.code(), value.status(),
                        value.feeRate(), value.remark(),
                        value.config() != null && !value.config().isBlank(), value.createTime());
    }
}
