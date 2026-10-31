package com.lemnt.system.application.auth.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.system.application.auth.cmd.MenuAddCmd;
import com.lemnt.system.application.auth.cmd.MenuUpdateCmd;
import com.lemnt.system.domain.auth.gateway.MenuGateway;
import com.lemnt.system.domain.auth.model.Menu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** 同级菜单名称冲突必须在执行器写入前拒绝。 */
class MenuNameUniqueTest {
    @Test
    void rejectsDuplicateNameBeforeAdding() {
        MenuGateway gateway = mock(MenuGateway.class);

        when(gateway.checkMenuNameUnique("测试菜单", 0L)).thenReturn(new Menu());

        assertThatThrownBy(() -> new MenuAddCmdExe(gateway).execute(new MenuAddCmd(
                "测试菜单", 0L, 1, "test", null, null, null, "1", "0", "M", "1", "2", null, "#")))
                .isInstanceOf(ServiceException.class)
                .hasMessage("新增菜单'测试菜单'失败，菜单名称已存在");

        verify(gateway, never()).insert(any());

    }

    @Test
    void rejectsDuplicateNameBeforeUpdating() {
        MenuGateway gateway = mock(MenuGateway.class);

        when(gateway.checkMenuNameUnique("测试菜单", 0L, 2L)).thenReturn(new Menu());

        assertThatThrownBy(() -> new MenuUpdateCmdExe(gateway).execute(updateCmd("测试菜单")))
                .isInstanceOf(ServiceException.class)
                .hasMessage("修改菜单'测试菜单'失败，菜单名称已存在");

        verify(gateway, never()).update(any());

    }

    @ParameterizedTest
    @ValueSource(strings = {
            "自身名称", "未占用名称"
            })
    void allowsOwnOrUnusedNameWhenUpdating(String name) {
        MenuGateway gateway = mock(MenuGateway.class);

        when(gateway.update(any())).thenReturn(true);

        assertThat(new MenuUpdateCmdExe(gateway).execute(updateCmd(name))).isTrue();

        verify(gateway).checkMenuNameUnique(name, 0L, 2L);

        ArgumentCaptor<Menu> entity = ArgumentCaptor.forClass(Menu.class);

        verify(gateway).update(entity.capture());

        assertThat(entity.getValue().getMenuId()).isEqualTo(2L);

        assertThat(entity.getValue().getMenuName()).isEqualTo(name);

    }

    @Test
    void allowsNameWithoutConflictInTargetParentWhenAdding() {
        MenuGateway gateway = mock(MenuGateway.class);

        when(gateway.insert(any())).thenReturn(true);

        assertThat(new MenuAddCmdExe(gateway).execute(new MenuAddCmd(
                "测试菜单", 3L, 1, "test", null, null, null, "1", "0", "M", "1", "2", null, "#"))).isTrue();

        verify(gateway).checkMenuNameUnique("测试菜单", 3L);

        ArgumentCaptor<Menu> entity = ArgumentCaptor.forClass(Menu.class);

        verify(gateway).insert(entity.capture());

        assertThat(entity.getValue().getParentId()).isEqualTo(3L);

    }

    /** 构造隐藏、停用菜单，覆盖同一父菜单下的修改。 */
    private MenuUpdateCmd updateCmd(String name) {
        return new MenuUpdateCmd(2L, name, 0L, 1, "test", null, null, null,
                "1", "0", "M", "1", "2", null, "#");

    }
}
