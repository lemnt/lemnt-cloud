package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.support.PayConfigRules;
import com.lemnt.trade.domain.payment.gateway.PayAppGateway;
import com.lemnt.trade.domain.payment.model.PayApp;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 启用或停用支付应用。 */
@Component
@RequiredArgsConstructor
public class PayAppStatusUpdateCmdExe {
    private final PayAppGateway apps;

    @Transactional(rollbackFor = Exception.class)
    public boolean execute(Long id, Integer status) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("支付应用编号不合法");
        }
        PayConfigRules.status(status);
        var existing = apps.find(id);
        if (existing == null) {
            throw new IllegalArgumentException("支付应用不存在");
        }
        return apps.update(new PayApp(existing.id(), existing.appKey(), existing.name(), status,
                existing.remark(), existing.orderNotifyUrl(), existing.refundNotifyUrl(), existing.createTime()));
    }
}
