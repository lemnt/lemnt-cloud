package com.lemnt.pay.infrastructure.channel.support;

import com.lemnt.pay.domain.channel.gateway.PayAppGateway;
import com.lemnt.pay.domain.channel.gateway.PayChannelConfigGateway;
import com.lemnt.pay.domain.channel.model.PayApp;
import com.lemnt.pay.domain.channel.model.PayChannelConfig;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 按支付单归属解析渠道配置：应用启停、渠道启停与唯一性由同一处判定。
 */
@Component
@RequiredArgsConstructor
public class PayChannelConfigResolver {
    private final PayAppGateway apps;
    private final PayChannelConfigGateway configs;

    /**
     * 匹配应用下唯一启用的渠道配置；应用或配置缺失、重复时返回空。
     *
     * @param appKey  支付应用标识
     * @param channel 渠道编码
     * @return 渠道配置，无法唯一确定时为 null
     */
    public PayChannelConfig resolve(String appKey, String channel) {
        if (appKey == null || appKey.isBlank() || channel == null || channel.isBlank()) {
            return null;
        }
        PayApp app = apps.findByAppKey(appKey);
        if (app == null || !Integer.valueOf(0).equals(app.status())) {
            return null;
        }
        var matching =
            configs.listByAppId(app.id()).stream()
                .filter(
                    config ->
                        channel.equals(config.code())
                            && Integer.valueOf(0).equals(config.status()))
                .toList();
        return matching.size() == 1 ? matching.get(0) : null;
    }
}
