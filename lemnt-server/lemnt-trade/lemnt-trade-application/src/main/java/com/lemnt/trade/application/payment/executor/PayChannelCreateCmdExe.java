package com.lemnt.trade.application.payment.executor;

import com.lemnt.trade.application.payment.cmd.PayChannelCreateCmd;
import com.lemnt.trade.application.payment.converter.PayConfigDTOConverter;
import com.lemnt.trade.application.payment.dto.PayChannelConfigDTO;
import com.lemnt.trade.application.payment.support.PayConfigRules;
import com.lemnt.trade.domain.payment.gateway.PayAppGateway;
import com.lemnt.trade.domain.payment.gateway.PayChannelConfigGateway;
import com.lemnt.trade.domain.payment.model.PayChannelConfig;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/** 新增支付渠道配置。 */
@Component
@RequiredArgsConstructor
public class PayChannelCreateCmdExe {
    private final PayAppGateway apps;
    private final PayChannelConfigGateway channels;

    @Transactional(rollbackFor = Exception.class)
    public PayChannelConfigDTO execute(PayChannelCreateCmd cmd) {
        if (cmd == null || cmd.appId() == null || cmd.appId() <= 0) {
            throw new IllegalArgumentException("所属应用不合法");
        }
        if (apps.find(cmd.appId()) == null) {
            throw new IllegalArgumentException("支付应用不存在");
        }
        String code = PayConfigRules.channelCode(cmd.code());
        PayConfigRules.status(cmd.status());
        if (channels.exists(cmd.appId(), code, null)) {
            throw new IllegalStateException("该应用下渠道编码已存在");
        }
        var value = new PayChannelConfig(null, cmd.appId(), code, cmd.status(),
                PayConfigRules.feeRate(cmd.feeRate()), PayConfigRules.optional(cmd.remark(), 255, "备注"),
                PayConfigRules.config(cmd.config()), null);
        return PayConfigDTOConverter.payChannel(channels.insert(value));
    }
}
