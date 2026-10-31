package com.lemnt.trade.application.payment.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.trade.application.payment.dto.PaymentChannelDTO;
import com.lemnt.trade.domain.payment.gateway.PayAppGateway;
import com.lemnt.trade.domain.payment.gateway.PayChannelConfigGateway;
import com.lemnt.trade.domain.payment.model.PaymentChannel;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 小程序收银台可用的支付渠道：仅返回应用下已启用的渠道配置。 */
@Component
@RequiredArgsConstructor
public class MemberPaymentChannelQueryExe {
    private final PayAppGateway apps;
    private final PayChannelConfigGateway channels;

    public List<PaymentChannelDTO> execute(String appKey) {
        if (appKey == null || appKey.isBlank() || appKey.length() > 64) {
            throw new ServiceException("支付应用不能为空", 400);
        }
        var app = apps.findByAppKey(appKey);
        if (app == null || !Integer.valueOf(0).equals(app.status())) {
            throw new ServiceException("支付应用不存在或已停用", 400);
        }
        return channels.listByAppId(app.id()).stream()
                .filter(value -> Integer.valueOf(0).equals(value.status()))
                .map(value -> channel(value.code()))
                .filter(value -> value != null)
                .distinct()
                .toList();
    }

    /** 渠道配置创建时已校验编码，这里仍对未知编码跳过，避免展示无法支付的渠道。 */
    private PaymentChannelDTO channel(String code) {
        try {
            var channel = PaymentChannel.valueOf(code);
            return new PaymentChannelDTO(channel.name(), channel.displayName());
        } catch (IllegalArgumentException | NullPointerException exception) {
            return null;
        }
    }
}
