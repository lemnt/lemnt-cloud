package com.lemnt.system.adapter.web.auth;

import com.lemnt.system.adapter.web.auth.converter.RoleReqConverter;
import com.lemnt.system.adapter.web.auth.req.RoleAddReq;
import com.lemnt.system.adapter.web.auth.req.RoleDataScopeUpdateReq;
import com.lemnt.system.adapter.web.auth.req.RoleUpdateReq;
import com.lemnt.system.adapter.web.auth.res.RoleRes;

import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoleContractTest {

    @Test
    void shouldExposeRoleContractsWithoutTreeSettings() {
        assertThat(fields(RoleAddReq.class)).containsExactly(
                "roleName", "roleKey", "roleSort", "dataScope", "status", "menuIds");
        assertThat(fields(RoleUpdateReq.class)).containsExactly(
                "roleId", "roleName", "roleKey", "roleSort", "dataScope", "status", "menuIds");
        assertThat(fields(RoleDataScopeUpdateReq.class)).containsExactly("roleId", "dataScope", "deptIds");
        assertThat(fields(RoleRes.class)).containsExactly(
                "roleId", "roleName", "roleKey", "roleSort", "dataScope", "status", "flag");
    }

    @Test
    void shouldConvertRoleAndScopeRequestsWithoutLosingSelectedIds() {
        var add = RoleReqConverter.toCmd(new RoleAddReq("运营", "operator", 2, "2", "1", List.of(10L, 11L)));
        assertThat(add.menuIds()).containsExactly(10L, 11L);
        assertThat(add.roleKey()).isEqualTo("operator");
        var update = RoleReqConverter.toCmd(new RoleUpdateReq(4L, "运营", "operator", 2, "2", "1", List.of(11L)));
        assertThat(update.roleId()).isEqualTo(4L);
        assertThat(update.menuIds()).containsExactly(11L);
        var scope = RoleReqConverter.toCmd(new RoleDataScopeUpdateReq(4L, "2", List.of(100L, 101L)));
        assertThat(scope.roleId()).isEqualTo(4L);
        assertThat(scope.dataScope()).isEqualTo("2");
        assertThat(scope.deptIds()).containsExactly(100L, 101L);
    }

    private List<String> fields(Class<?> type) {
        return Arrays.stream(type.getRecordComponents()).map(RecordComponent::getName).toList();
    }
}
