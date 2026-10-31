package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.cmd.PayChannelUpdateCmd;
import com.lemnt.trade.application.payment.support.PayConfigRules;
import com.lemnt.trade.domain.payment.gateway.PayChannelConfigGateway;
import com.lemnt.trade.domain.payment.model.PayChannelConfig;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 修改支付渠道配置，所属应用与渠道编码保持不变。 */
@Component
@RequiredArgsConstructor
public class PayChannelUpdateCmdExe {
    private final PayChannelConfigGateway channels;

    @Transactional(rollbackFor = Exception.class)
    public boolean execute(PayChannelUpdateCmd cmd) {
        if (cmd == null || cmd.id() == null || cmd.id() <= 0) {
            throw new IllegalArgumentException("渠道配置编号不合法");
        }
        var existing = channels.find(cmd.id());
        if (existing == null) {
            throw new IllegalArgumentException("渠道配置不存在");
        }
        PayConfigRules.status(cmd.status());
        // 渠道配置含密钥：留空表示保留原值，只有显式传入才替换。
        String config = cmd.config() == null || cmd.config().isBlank()
                ? existing.config()
                : PayConfigRules.config(cmd.config());
        var value = new PayChannelConfig(existing.id(), existing.appId(), existing.code(), cmd.status(),
                PayConfigRules.feeRate(cmd.feeRate()), PayConfigRules.optional(cmd.remark(), 255, "备注"),
                config, existing.createTime());
        return channels.update(value);
    }
}
