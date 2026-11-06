package com.lemnt.member.start;

import com.lemnt.member.domain.growth.model.Config;
import com.lemnt.member.infrastructure.growth.gateway.MemberConfigGatewayImpl;
import com.lemnt.system.api.RemoteConfigService;

import com.alibaba.fastjson2.JSON;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 真实业务适配器验证整组JSON，缺失或RPC故障不得伪造默认配置。
 */
class MemberConfigStorageTest {
    @Test
    void roundTripsSnapshotAndPropagatesMissingAndTransportFailure() {
        var remote = mock(RemoteConfigService.class);
        var gateway = new MemberConfigGatewayImpl();
        ReflectionTestUtils.setField(gateway, "remoteConfigService", remote);
        assertNull(gateway.config());
        var value = new Config(true, 5, 2, 3);
        when(remote.getConfig("member.point.config")).thenReturn(JSON.toJSONString(value));
        assertEquals(value, gateway.config());
        gateway.configSave(value);
        verify(remote)
            .saveBusinessConfig(
                argThat(
                    bo ->
                        bo.configKey().equals("member.point.config")
                            && JSON.parseObject(bo.configValue(), Config.class)
                            .equals(value)));
        when(remote.getConfig("member.point.config"))
            .thenThrow(new IllegalStateException("system unavailable"));
        assertThrows(IllegalStateException.class, gateway::config);
        doThrow(new IllegalStateException("system unavailable"))
            .when(remote)
            .saveBusinessConfig(any());
        assertThrows(IllegalStateException.class, () -> gateway.configSave(value));
    }
}
