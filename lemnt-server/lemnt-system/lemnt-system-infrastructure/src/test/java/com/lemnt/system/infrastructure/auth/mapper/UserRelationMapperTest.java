package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.system.infrastructure.auth.dataobject.UserPostDO;
import com.lemnt.system.infrastructure.auth.dataobject.UserRoleDO;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

class UserRelationMapperTest {

    @BeforeAll
    static void initializeTableMetadata() {
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "test");
        assistant.setCurrentNamespace("test");
        TableInfoHelper.initTableInfo(assistant, UserRoleDO.class);
        TableInfoHelper.initTableInfo(assistant, UserPostDO.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldBindEachUserIdWhenDeletingRoles(boolean multiple) {
        UserRoleMapper mapper = mock(UserRoleMapper.class, CALLS_REAL_METHODS);
        doReturn(1).when(mapper).delete(any());
        List<Long> ids = multiple ? List.of(2L, 3L) : List.of(2L);

        assertThat(mapper.deleteUserRoleByUserIds(ids)).isEqualTo(1);

        ArgumentCaptor<LambdaQueryWrapper<UserRoleDO>> wrapper = ArgumentCaptor.captor();
        verify(mapper).delete(wrapper.capture());
        assertUserIdBindings(wrapper.getValue(), ids);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldBindEachUserIdAndRoleWhenRevokingRoles(boolean multiple) {
        UserRoleMapper mapper = mock(UserRoleMapper.class, CALLS_REAL_METHODS);
        doReturn(1).when(mapper).delete(any());
        List<Long> ids = multiple ? List.of(2L, 3L) : List.of(2L);

        assertThat(mapper.deleteUserRoleInfos(10L, ids)).isEqualTo(1);

        ArgumentCaptor<LambdaQueryWrapper<UserRoleDO>> wrapper = ArgumentCaptor.captor();
        verify(mapper).delete(wrapper.capture());
        assertThat(wrapper.getValue().getSqlSegment()).contains("role_id =", "user_id IN");
        assertThat(wrapper.getValue().getParamNameValuePairs().values())
                .containsExactlyInAnyOrderElementsOf(multiple ? List.of(10L, 2L, 3L) : List.of(10L, 2L));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldBindEachUserIdWhenDeletingPosts(boolean multiple) {
        UserPostMapper mapper = mock(UserPostMapper.class, CALLS_REAL_METHODS);
        doReturn(1).when(mapper).delete(any());
        List<Long> ids = multiple ? List.of(2L, 3L) : List.of(2L);

        assertThat(mapper.deleteUserPostByUserIds(ids)).isEqualTo(1);

        ArgumentCaptor<LambdaQueryWrapper<UserPostDO>> wrapper = ArgumentCaptor.captor();
        verify(mapper).delete(wrapper.capture());
        assertUserIdBindings(wrapper.getValue(), ids);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldSkipDeletesForNullOrEmptyUserIds(boolean nullIds) {
        UserRoleMapper roles = mock(UserRoleMapper.class, CALLS_REAL_METHODS);
        UserPostMapper posts = mock(UserPostMapper.class, CALLS_REAL_METHODS);
        List<Long> ids = nullIds ? null : List.of();

        assertThat(roles.deleteUserRoleByUserIds(ids)).isZero();
        assertThat(roles.deleteUserRoleInfos(10L, ids)).isZero();
        assertThat(posts.deleteUserPostByUserIds(ids)).isZero();

        verify(roles, never()).delete(any());
        verify(posts, never()).delete(any());
    }

    private void assertUserIdBindings(LambdaQueryWrapper<?> wrapper, List<Long> ids) {
        assertThat(wrapper.getSqlSegment()).contains("user_id IN");
        assertThat(wrapper.getParamNameValuePairs().values()).containsExactlyInAnyOrderElementsOf(ids);
    }
}
