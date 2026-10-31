package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.cmd.PayAppCreateCmd;
import com.lemnt.trade.application.payment.converter.PayConfigDTOConverter;
import com.lemnt.trade.application.payment.dto.PayAppDTO;
import com.lemnt.trade.application.payment.support.PayConfigRules;
import com.lemnt.trade.domain.payment.gateway.PayAppGateway;
import com.lemnt.trade.domain.payment.model.PayApp;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 新增支付应用。 */
@Component
@RequiredArgsConstructor
public class PayAppCreateCmdExe {
    private final PayAppGateway apps;

    @Transactional(rollbackFor = Exception.class)
    public PayAppDTO execute(PayAppCreateCmd cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("支付应用参数不合法");
        }
        String appKey = PayConfigRules.appKey(cmd.appKey());
        PayConfigRules.status(cmd.status());
        if (apps.existsAppKey(appKey, null)) {
            throw new IllegalStateException("应用标识已存在");
        }
        var value = new PayApp(null, appKey, PayConfigRules.required(cmd.name(), 64, "应用名"), cmd.status(),
                PayConfigRules.optional(cmd.remark(), 255, "备注"),
                PayConfigRules.url(cmd.orderNotifyUrl(), "支付结果回调地址"),
                PayConfigRules.url(cmd.refundNotifyUrl(), "退款结果回调地址"), null);
        return PayConfigDTOConverter.payApp(apps.insert(value));
    }
}
