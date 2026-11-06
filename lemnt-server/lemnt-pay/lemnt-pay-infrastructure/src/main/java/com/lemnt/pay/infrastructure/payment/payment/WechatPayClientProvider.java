package com.lemnt.pay.infrastructure.payment.payment;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.core.utils.JsonUtils;
import com.lemnt.pay.domain.payment.model.PayChannelConfig;
import com.lemnt.third.sdk.wechatpay.WechatPayClient;
import com.lemnt.third.sdk.wechatpay.config.WechatPayConfig;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 按渠道配置缓存微信支付客户端；官方 SDK 创建时会下载平台证书，不能每次请求重建。
 */
@Component
public class WechatPayClientProvider {
    private final Map<String, WechatPayClient> clients = new ConcurrentHashMap<>();

    /**
     * 获取客户端；配置内容变化时重建，避免沿用旧商户号或旧私钥。
     *
     * @param config 支付渠道配置
     * @return 微信支付客户端
     */
    public WechatPayClient get(PayChannelConfig config) {
        if (config == null || config.config() == null || config.config().isBlank()) {
            throw new ServiceException("微信支付渠道配置不能为空", 400);
        }
        String key = config.id() + ":" + config.config();
        return clients.computeIfAbsent(key, ignored -> create(config.config()));
    }

    private WechatPayClient create(String value) {
        WechatPayConfig channelConfig = JsonUtils.toBean(value, WechatPayConfig.class);
        if (channelConfig == null) {
            throw new ServiceException("微信支付渠道配置不是合法JSON", 400);
        }
        try {
            return WechatPayClient.of(channelConfig);
        } catch (IllegalArgumentException exception) {
            throw new ServiceException(exception.getMessage(), 400);
        }
    }
}
