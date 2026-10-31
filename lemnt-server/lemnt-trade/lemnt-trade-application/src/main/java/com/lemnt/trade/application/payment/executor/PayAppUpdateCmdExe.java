package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.cmd.PayAppUpdateCmd;
import com.lemnt.trade.application.payment.support.PayConfigRules;
import com.lemnt.trade.domain.payment.gateway.PayAppGateway;
import com.lemnt.trade.domain.payment.model.PayApp;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 修改支付应用，应用标识保持不变。 */
@Component
@RequiredArgsConstructor
public class PayAppUpdateCmdExe {
    private final PayAppGateway apps;

    @Transactional(rollbackFor = Exception.class)
    public boolean execute(PayAppUpdateCmd cmd) {
        if (cmd == null || cmd.id() == null || cmd.id() <= 0) {
            throw new IllegalArgumentException("支付应用编号不合法");
        }
        var existing = apps.find(cmd.id());
        if (existing == null) {
            throw new IllegalArgumentException("支付应用不存在");
        }
        PayConfigRules.status(cmd.status());
        var value = new PayApp(existing.id(), existing.appKey(), PayConfigRules.required(cmd.name(), 64, "应用名"),
                cmd.status(), PayConfigRules.optional(cmd.remark(), 255, "备注"),
                PayConfigRules.url(cmd.orderNotifyUrl(), "支付结果回调地址"),
                PayConfigRules.url(cmd.refundNotifyUrl(), "退款结果回调地址"), existing.createTime());
        return apps.update(value);
    }
}
