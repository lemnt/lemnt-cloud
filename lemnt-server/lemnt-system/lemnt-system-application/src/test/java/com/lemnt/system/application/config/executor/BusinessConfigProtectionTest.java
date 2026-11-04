package com.lemnt.system.application.config.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.system.api.BusinessConfigKeys;
import com.lemnt.system.api.bo.BusinessConfigSaveBo;
import com.lemnt.system.application.config.cmd.ConfigAddCmd;
import com.lemnt.system.application.config.cmd.ConfigUpdateCmd;
import com.lemnt.system.application.dubbo.RemoteConfigServiceImpl;
import com.lemnt.system.domain.config.gateway.ConfigGateway;
import com.lemnt.system.domain.config.model.Config;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * 通用配置入口不能绕过业务权限与关联参数校验，RPC只允许保存完整业务组。
 */
class BusinessConfigProtectionTest {
    final ConfigGateway gateway = mock(ConfigGateway.class);

    @Test
    void rejectsGenericAddUpdateRenameAndMixedDeletionBeforeWriting() {
        for (String key :
            List.of(
                BusinessConfigKeys.MEMBER_POINT,
                BusinessConfigKeys.TRADE,
                "TRADE.CONFIG ")) {
            assertThrows(
                ServiceException.class,
                () ->
                    new ConfigAddCmdExe(gateway)
                        .execute(new ConfigAddCmd("配置", key, "{}", "1", null)));
            assertThrows(
                ServiceException.class,
                () ->
                    new ConfigUpdateCmdExe(gateway)
                        .execute(new ConfigUpdateCmd(1L, "配置", key, "{}", "1", null)));
        }
        var reserved = new Config();
        reserved.setConfigKey(BusinessConfigKeys.TRADE);
        when(gateway.selectById(2L)).thenReturn(reserved);
        assertThrows(
            ServiceException.class,
            () ->
                new ConfigUpdateCmdExe(gateway)
                    .execute(new ConfigUpdateCmd(2L, "重命名", "other", "{}", "0", null)));
        assertThrows(
            ServiceException.class,
            () -> new ConfigDeleteCmdExe(gateway).execute(List.of(1L, 2L)));
        verify(gateway, never()).insert(any());
        verify(gateway, never()).update(any());
        verify(gateway, never()).deleteByIds(any());
    }

    @Test
    void internalRpcRejectsOtherKeysAndNonObjectJson() {
        var service = new RemoteConfigServiceImpl(gateway);
        assertThrows(ServiceException.class, () -> service.saveBusinessConfig(null));
        assertThrows(
            ServiceException.class,
            () ->
                service.saveBusinessConfig(
                    new BusinessConfigSaveBo("other", "{}", "operator")));
        for (String value : List.of("[]", "null", "broken")) {
            assertThrows(
                ServiceException.class,
                () ->
                    service.saveBusinessConfig(
                        new BusinessConfigSaveBo(
                            BusinessConfigKeys.TRADE, value, "operator")));
        }
        verifyNoInteractions(gateway);
        service.saveBusinessConfig(
            new BusinessConfigSaveBo(BusinessConfigKeys.TRADE, "{\"id\":1}", "operator"));
        verify(gateway)
            .saveBusinessConfig(
                argThat(
                    value ->
                        value.getConfigKey().equals(BusinessConfigKeys.TRADE)
                            && value.getUpdateBy().equals("operator")));
    }
}
