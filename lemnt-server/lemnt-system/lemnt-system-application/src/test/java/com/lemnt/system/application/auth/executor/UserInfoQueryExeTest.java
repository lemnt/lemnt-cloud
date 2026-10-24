package com.lemnt.system.application.auth.executor;

import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.system.domain.auth.gateway.MenuGateway;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.gateway.UserGateway;
import com.lemnt.system.domain.auth.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 验证框架初始化信息仅包含用户、角色和权限。 */
class UserInfoQueryExeTest {
    private final UserGateway users = mock(UserGateway.class);
    private final RoleGateway roles = mock(RoleGateway.class);
    private final MenuGateway menus = mock(MenuGateway.class);
    private final UserInfoQueryExe executor = new UserInfoQueryExe(users, roles, menus);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.remove();
    }

    @Test
    void shouldReturnUserRolesAndPermissions() {
        SecurityContextHolder.setUserId("101");
        User user = new User().setUserId(101L);
        when(users.selectUserById(101L)).thenReturn(user);
        when(roles.selectRolePermissionByUserId(101L)).thenReturn(Set.of("common"));
        when(menus.selectMenuPermsByUserId(101L)).thenReturn(List.of("system:user:list"));

        var result = executor.execute();

        assertThat(result).containsOnlyKeys("user", "roles", "permissions");
        assertThat(result.get("user")).isSameAs(user);
        assertThat(result.get("roles")).isEqualTo(Set.of("common"));
        assertThat(result.get("permissions")).isEqualTo(Set.of("system:user:list"));
    }
}
