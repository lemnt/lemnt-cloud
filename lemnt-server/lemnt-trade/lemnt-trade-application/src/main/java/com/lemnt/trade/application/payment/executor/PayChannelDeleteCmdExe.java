package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.domain.payment.gateway.PayChannelConfigGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 删除支付渠道配置。 */
@Component
@RequiredArgsConstructor
public class PayChannelDeleteCmdExe {
    private final PayChannelConfigGateway channels;

    @Transactional(rollbackFor = Exception.class)
    public boolean execute(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("渠道配置编号不合法");
        }
        if (channels.find(id) == null) {
            throw new IllegalArgumentException("渠道配置不存在");
        }
        return channels.delete(id);
    }
}
