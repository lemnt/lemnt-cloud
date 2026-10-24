package com.lemnt.system.application.auth.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.system.application.auth.service.TreeMenuService;
import com.lemnt.system.domain.auth.gateway.MenuGateway;
import com.lemnt.system.domain.auth.gateway.RoleGateway;
import com.lemnt.system.domain.auth.model.Menu;
import com.lemnt.system.domain.auth.model.Role;
import com.lemnt.system.domain.auth.service.RoleDomainService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** 角色授权树必须先校验角色数据范围，再读取选中菜单。 */
class MenuRoleTreeQueryExeTest {
    private final RoleGateway roles = mock(RoleGateway.class);
    private final MenuGateway menus = mock(MenuGateway.class);
    private final MenuRoleTreeQueryExe executor = new MenuRoleTreeQueryExe(
            menus, new TreeMenuService(), new RoleDomainService(roles));

    @AfterEach
    void clearContext() {
        SecurityContextHolder.remove();
    }

    @Test
    void shouldRejectRoleOutsideVisibleScopeBeforeReadingMenus() {
        when(roles.selectRoleList(any(Role.class))).thenReturn(List.of());

        assertThatThrownBy(() -> executor.execute(103L))
                .isInstanceOf(ServiceException.class).hasMessageContaining("没有权限访问角色数据");
        verifyNoInteractions(menus);
    }

    @Test
    void shouldReturnAllowedRoleSelectionAndCurrentUserMenuTree() {
        SecurityContextHolder.setUserId("100");
        when(roles.selectRoleList(any(Role.class))).thenReturn(List.of(new Role().setRoleId(100L)));
        when(menus.selectListByUserId(any(Menu.class))).thenReturn(List.of());
        when(menus.selectMenuListByRoleId(100L)).thenReturn(List.of(11L, 12L));

        var result = executor.execute(100L);

        assertThat(result.get("checkedKeys")).isEqualTo(List.of(11L, 12L));
        ArgumentCaptor<Role> role = ArgumentCaptor.forClass(Role.class);
        verify(roles).selectRoleList(role.capture());
        assertThat(role.getValue().getRoleId()).isEqualTo(100L);
        ArgumentCaptor<Menu> menu = ArgumentCaptor.forClass(Menu.class);
        verify(menus).selectListByUserId(menu.capture());
        assertThat(menu.getValue().getUserId()).isEqualTo(100L);
    }
}
