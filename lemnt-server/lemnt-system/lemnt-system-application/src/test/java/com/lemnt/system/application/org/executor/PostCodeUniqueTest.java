package com.lemnt.system.application.org.executor;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.utils.SecurityUtils;
import com.lemnt.system.application.org.cmd.PostAddCmd;
import com.lemnt.system.application.org.cmd.PostUpdateCmd;
import com.lemnt.system.domain.org.gateway.PostGateway;
import com.lemnt.system.domain.org.model.Post;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 岗位新增和修改均需在写入前拒绝重复编码。 */
class PostCodeUniqueTest {

    private final PostGateway gateway = mock(PostGateway.class);

    @Test
    void shouldRejectDuplicateWhenAdding() {
        when(gateway.checkPostCodeUnique("test")).thenReturn(new Post());

        assertThatThrownBy(() -> new PostAddCmdExe(gateway)
                .execute(new PostAddCmd("测试", "test", 0, "2", "备注")))
                .isInstanceOf(ServiceException.class).hasMessageContaining("岗位编码已存在");

        verify(gateway, never()).insert(any());

    }

    @Test
    void shouldRejectOtherPostWithSameCodeWhenUpdating() {
        when(gateway.checkPostCodeUnique("test", 5L)).thenReturn(new Post());

        assertThatThrownBy(() -> new PostUpdateCmdExe(gateway)
                .execute(new PostUpdateCmd(5L, "测试", "test", 0, "2", "备注")))
                .isInstanceOf(ServiceException.class).hasMessageContaining("岗位编码已存在");

        verify(gateway, never()).update(any());

    }

    @Test
    void shouldAllowUnusedCodeWhenAdding() {
        try (var security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getUsername).thenReturn("admin");

            when(gateway.insert(any())).thenReturn(true);

            assertThat(new PostAddCmdExe(gateway)
                    .execute(new PostAddCmd("测试", "test", 0, "2", "备注"))).isTrue();

            verify(gateway).checkPostCodeUnique("test");

            verify(gateway).insert(any());

        }
    }

    @Test
    void shouldExcludeSelfWhenUpdating() {
        try (var security = mockStatic(SecurityUtils.class)) {
            security.when(SecurityUtils::getUsername).thenReturn("admin");

            when(gateway.update(any())).thenReturn(true);

            assertThat(new PostUpdateCmdExe(gateway)
                    .execute(new PostUpdateCmd(5L, "测试", "test", 0, "2", "备注"))).isTrue();

            verify(gateway).checkPostCodeUnique("test", 5L);

            verify(gateway).update(any());

        }
    }
}
