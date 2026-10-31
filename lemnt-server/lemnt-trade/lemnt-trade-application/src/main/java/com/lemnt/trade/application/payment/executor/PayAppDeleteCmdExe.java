package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.domain.payment.gateway.PayAppGateway;
import com.lemnt.trade.domain.payment.gateway.PayChannelConfigGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 删除支付应用，仍有渠道配置时拒绝。 */
@Component
@RequiredArgsConstructor
public class PayAppDeleteCmdExe {
    private final PayAppGateway apps;
    private final PayChannelConfigGateway channels;

    @Transactional(rollbackFor = Exception.class)
    public boolean execute(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("支付应用编号不合法");
        }
        if (apps.find(id) == null) {
            throw new IllegalArgumentException("支付应用不存在");
        }
        if (channels.channelReferenced(id)) {
            throw new IllegalStateException("应用下仍有渠道配置，不能删除");
        }
        return apps.delete(id);
    }
}
