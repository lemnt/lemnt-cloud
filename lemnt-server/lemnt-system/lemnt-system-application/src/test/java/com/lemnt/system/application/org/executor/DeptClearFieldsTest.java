package com.lemnt.system.application.org.executor;

import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.org.cmd.DeptUpdateCmd;
import com.lemnt.system.domain.org.gateway.DeptGateway;
import com.lemnt.system.domain.org.model.Dept;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 完整部门编辑将空联系字段传给持久化层，同时保留原层级和状态。 */
class DeptClearFieldsTest {
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "原值")
    void clearsNullFieldsAndPreservesPopulatedFields(String value) {
        DeptGateway gateway = mock(DeptGateway.class);
        when(gateway.updateDept(any())).thenReturn(true);
        try (var security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::isAdmin).thenReturn(true);
            assertThat(new DeptUpdateCmdExe(gateway).execute(new DeptUpdateCmd(
                    200L, 100L, "0,100", "测试部门", 97, value, value, value, "2"))).isTrue();
        }
        ArgumentCaptor<Dept> entity = ArgumentCaptor.forClass(Dept.class);
        verify(gateway).updateDept(entity.capture());
        String expected = value == null ? "" : value;
        assertThat(entity.getValue().getLeader()).isEqualTo(expected);
        assertThat(entity.getValue().getPhone()).isEqualTo(expected);
        assertThat(entity.getValue().getEmail()).isEqualTo(expected);
        assertThat(entity.getValue().getParentId()).isEqualTo(100L);
        assertThat(entity.getValue().getStatus()).isEqualTo("2");
    }
}
