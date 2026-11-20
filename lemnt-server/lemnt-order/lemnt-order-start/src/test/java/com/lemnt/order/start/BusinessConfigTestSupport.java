package com.lemnt.order.start;

import com.lemnt.order.domain.config.model.TradeConfig;
import com.lemnt.order.infrastructure.config.gateway.TradeConfigGatewayImpl;
import com.lemnt.system.api.RemoteConfigService;
import com.lemnt.system.api.bo.BusinessConfigSaveBo;

import com.alibaba.fastjson2.JSON;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 仅替代Dubbo传输；生产Gateway/JSON和业务服务仍真实执行，system存储另有集成测试。
 */
public final class BusinessConfigTestSupport {
    public static TradeConfigGatewayImpl gateway() {
        var values = new ConcurrentHashMap<String, String>();
        values.put(
            "trade.config",
            JSON.toJSONString(
                new TradeConfig(
                    1L, List.of(), List.of(), false, 0, false, false, 1, 1, List.of(),
                    0, 0, 0, 0, 0, List.of())));
        var remote =
            new RemoteConfigService() {
                public String getConfig(String key) {
                    return values.get(key);
                }

                public void saveBusinessConfig(BusinessConfigSaveBo value) {
                    values.put(value.configKey(), value.configValue());
                }
            };
        var gateway = new TradeConfigGatewayImpl();
        ReflectionTestUtils.setField(gateway, "remoteConfigService", remote);
        return gateway;
    }
}
