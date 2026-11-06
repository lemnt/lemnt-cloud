package com.lemnt.pay.infrastructure.channel.factory;

import com.lemnt.pay.infrastructure.channel.adapter.AbstractChannelAdapter;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 支付渠道工厂：Spring 注入全部渠道实现，按渠道编码建立索引。
 * 重复实现在启动期失败，未接入渠道在请求期失败；新增渠道只需实现接口并由容器注册。
 */
@Component
public class PaymentChannelFactory {

    private final Map<String, AbstractChannelAdapter> channelMap;

    public PaymentChannelFactory(List<AbstractChannelAdapter> adapters) {
        Map<String, AbstractChannelAdapter> map = new HashMap<>();
        for (AbstractChannelAdapter adapter : adapters) {
            String code = adapter.channel();
            AbstractChannelAdapter existing = map.putIfAbsent(code, adapter);
            if (existing != null) {
                throw new IllegalStateException("重复的支付渠道编码: " + code);
            }
        }
        this.channelMap = Collections.unmodifiableMap(map);
    }

    public AbstractChannelAdapter getChannel(String channelCode) {
        AbstractChannelAdapter adapter = channelMap.get(channelCode);
        if (adapter == null) {
            throw new IllegalArgumentException("不支持的支付渠道：" + channelCode);
        }
        return adapter;
    }

}
