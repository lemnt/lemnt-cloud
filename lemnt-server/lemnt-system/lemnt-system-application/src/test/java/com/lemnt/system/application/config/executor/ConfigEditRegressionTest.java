package com.lemnt.system.application.config.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.config.cmd.ConfigAddCmd;
import com.lemnt.system.application.config.cmd.ConfigUpdateCmd;
import com.lemnt.system.application.config.cmd.DictDataUpdateCmd;
import com.lemnt.system.application.config.cmd.DictTypeAddCmd;
import com.lemnt.system.application.config.cmd.DictTypeUpdateCmd;
import com.lemnt.system.domain.config.gateway.ConfigGateway;
import com.lemnt.system.domain.config.gateway.DictDataGateway;
import com.lemnt.system.domain.config.gateway.DictTypeGateway;
import com.lemnt.system.domain.config.model.Config;
import com.lemnt.system.domain.config.model.DictData;
import com.lemnt.system.domain.config.model.DictType;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 完整编辑必须清除可选字段，重复键名和类型必须在写入前给出业务提示。 */
class ConfigEditRegressionTest {

    @Test
    void rejectsDuplicateConfigKeyWhenAddingDirectly() {
        ConfigGateway gateway = mock(ConfigGateway.class);

        when(gateway.checkConfigKeyUnique("test")).thenReturn(new Config());

        try (var security = mockStatic(SecurityUtils.class)) {
            assertThatThrownBy(() -> new ConfigAddCmdExe(gateway)
                    .execute(new ConfigAddCmd("测试", "test", "value", "0", "备注")))
                    .isInstanceOf(ServiceException.class)
                    .hasMessage("新增参数'测试'失败，参数键名已存在");

        }
        verify(gateway, never()).insert(any());

    }

    @Test
    void allowsUnusedConfigKeyWhenAdding() {
        ConfigGateway gateway = mock(ConfigGateway.class);

        when(gateway.insert(any())).thenReturn(true);

        try (var security = mockStatic(SecurityUtils.class)) {
            assertThat(new ConfigAddCmdExe(gateway)
                    .execute(new ConfigAddCmd("测试", "unused", "value", "0", "备注"))).isTrue();

        }
        verify(gateway).checkConfigKeyUnique("unused");

        ArgumentCaptor<Config> entity = ArgumentCaptor.forClass(Config.class);

        verify(gateway).insert(entity.capture());

        assertThat(entity.getValue().getConfigKey()).isEqualTo("unused");

        assertThat(entity.getValue().getConfigValue()).isEqualTo("value");

        assertThat(entity.getValue().getRemark()).isEqualTo("备注");

    }

    @Test
    void rejectsDuplicateConfigKeyBeforeUpdating() {
        ConfigGateway gateway = mock(ConfigGateway.class);

        Config existing = new Config();

        existing.setConfigId(1L);

        when(gateway.checkConfigKeyUnique("test", 2L)).thenReturn(existing);

        assertThatThrownBy(() -> new ConfigUpdateCmdExe(gateway)
                .execute(new ConfigUpdateCmd(2L, "测试", "test", "value", "0", null)))
                .isInstanceOf(ServiceException.class).hasMessageContaining("参数键名已存在");

        verify(gateway, never()).update(any());

    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "保留备注")
    void keepsOwnKeyAndWritesExplicitRemark(String remark) {
        ConfigGateway gateway = mock(ConfigGateway.class);

        when(gateway.update(any())).thenReturn(true);

        try (var security = mockStatic(SecurityUtils.class)) {
            assertThat(new ConfigUpdateCmdExe(gateway)
                    .execute(new ConfigUpdateCmd(2L, "测试", "test", "value", "0", remark))).isTrue();

        }
        ArgumentCaptor<Config> entity = ArgumentCaptor.forClass(Config.class);

        verify(gateway).update(entity.capture());

        verify(gateway).checkConfigKeyUnique("test", 2L);

        assertThat(entity.getValue().getRemark()).isEqualTo(remark == null ? "" : remark);

        assertThat(entity.getValue().getConfigValue()).isEqualTo("value");

    }

    @Test
    void rejectsDuplicateTypeWhenAdding() {
        DictTypeGateway gateway = mock(DictTypeGateway.class);

        when(gateway.checkDictTypeUnique("test")).thenReturn(new DictType());

        assertThatThrownBy(() -> new DictTypeAddCmdExe(gateway)
                .execute(new DictTypeAddCmd("测试", "test", "2", null)))
                .isInstanceOf(ServiceException.class).hasMessageContaining("字典类型已存在");

        verify(gateway, never()).insert(any());

    }

    @Test
    void rejectsOtherTypeWhenUpdating() {
        DictTypeGateway gateway = mock(DictTypeGateway.class);

        DictType existing = new DictType();

        existing.setDictId(1L);

        when(gateway.checkDictTypeUnique("test")).thenReturn(existing);

        assertThatThrownBy(() -> new DictTypeUpdateCmdExe(gateway,
                org.mockito.Mockito.mock(com.lemnt.system.domain.config.gateway.DictDataGateway.class))
                .execute(new DictTypeUpdateCmd(2L, "测试", "test", "2", null)))
                .isInstanceOf(ServiceException.class).hasMessageContaining("字典类型已存在");

        verify(gateway, never()).update(any());

    }

    @Test
    void allowsSameTypeAndUnusedType() {
        DictTypeGateway gateway = mock(DictTypeGateway.class);

        DictType existing = new DictType();

        existing.setDictId(2L);

        when(gateway.checkDictTypeUnique("test")).thenReturn(existing);

        when(gateway.update(any())).thenReturn(true);

        when(gateway.insert(any())).thenReturn(true);

        try (var security = mockStatic(SecurityUtils.class)) {
            assertThat(new DictTypeUpdateCmdExe(gateway,
                    org.mockito.Mockito.mock(com.lemnt.system.domain.config.gateway.DictDataGateway.class))
                    .execute(new DictTypeUpdateCmd(2L, "测试", "test", "2", "备注"))).isTrue();

            assertThat(new DictTypeAddCmdExe(gateway)
                    .execute(new DictTypeAddCmd("新测试", "unused", "2", "备注"))).isTrue();

        }
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "原值")
    void writesClearedOrPopulatedDataFields(String value) {
        DictDataGateway gateway = mock(DictDataGateway.class);

        when(gateway.update(any())).thenReturn(true);

        try (var security = mockStatic(SecurityUtils.class)) {
            assertThat(new DictDataUpdateCmdExe(gateway).execute(new DictDataUpdateCmd(
                    3L, 0L, "标签", "0", "test", value, "default", "0", "2", value))).isTrue();

        }
        ArgumentCaptor<DictData> entity = ArgumentCaptor.forClass(DictData.class);

        verify(gateway).update(entity.capture());

        assertThat(entity.getValue().getCssClass()).isEqualTo(value == null ? "" : value);

        assertThat(entity.getValue().getRemark()).isEqualTo(value == null ? "" : value);

        assertThat(entity.getValue().getDictValue()).isEqualTo("0");

    }
}
