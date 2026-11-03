package com.lemnt.trade.infrastructure.config;

import com.lemnt.system.api.RemoteConfigService;
import com.lemnt.trade.domain.config.model.TradeConfig;
import com.lemnt.trade.infrastructure.config.gateway.TradeConfigGatewayImpl;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 真实业务适配器验证整组JSON，缺失或RPC故障不得伪造默认配置。 */
class TradeConfigStorageTest {
    @Test
    void roundTripsSnapshotAndPropagatesMissingAndTransportFailure() {
        var remote = mock(RemoteConfigService.class);
        var gateway = new TradeConfigGatewayImpl();
        ReflectionTestUtils.setField(gateway, "remoteConfigService", remote);
        assertNull(gateway.get());
        var value =
                new TradeConfig(
                        1L,
                        java.util.List.of("原因"),
                        java.util.List.of(),
                        false,
                        1000,
                        true,
                        true,
                        1,
                        2,
                        java.util.List.of(),
                        10,
                        5,
                        100,
                        2,
                        3,
                        java.util.List.of(1));
        when(remote.getConfig("trade.config")).thenReturn(JSON.toJSONString(value));
        assertEquals(value, gateway.get());
        gateway.update(value);
        verify(remote)
                .saveBusinessConfig(
                        argThat(
                                bo ->
                                        bo.configKey().equals("trade.config")
                                                && JSON.parseObject(
                                                                bo.configValue(), TradeConfig.class)
                                                        .equals(value)));
        when(remote.getConfig("trade.config"))
                .thenThrow(new IllegalStateException("system unavailable"));
        assertThrows(IllegalStateException.class, gateway::get);
        doThrow(new IllegalStateException("system unavailable"))
                .when(remote)
                .saveBusinessConfig(any());
        assertThrows(IllegalStateException.class, () -> gateway.update(value));
    }
}
