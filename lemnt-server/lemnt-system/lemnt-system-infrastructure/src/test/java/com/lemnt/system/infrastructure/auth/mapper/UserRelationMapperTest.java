package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.system.infrastructure.auth.dataobject.UserPostDO;
import com.lemnt.system.infrastructure.auth.dataobject.UserRoleDO;
import com.lemnt.system.infrastructure.auth.gateway.UserPostGatewayImpl;
import com.lemnt.system.infrastructure.auth.gateway.UserRoleGatewayImpl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class UserRelationMapperTest {

    @BeforeAll
    static void initializeTableMetadata() {
        MapperBuilderAssistant assistant =
            new MapperBuilderAssistant(new MybatisConfiguration(), "test");

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

        assertThat(new UserRoleGatewayImpl(mapper).deleteUserRoleByUserIds(ids)).isTrue();

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

        assertThat(new UserRoleGatewayImpl(mapper).deleteUserRoleInfosByRoleId(10L, ids)).isTrue();

        ArgumentCaptor<LambdaQueryWrapper<UserRoleDO>> wrapper = ArgumentCaptor.captor();

        verify(mapper).delete(wrapper.capture());

        assertThat(wrapper.getValue().getSqlSegment()).contains("role_id =", "user_id IN");

        assertThat(wrapper.getValue().getParamNameValuePairs().values())
            .containsExactlyInAnyOrderElementsOf(
                multiple ? List.of(10L, 2L, 3L) : List.of(10L, 2L));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldBindEachUserIdWhenDeletingPosts(boolean multiple) {
        UserPostMapper mapper = mock(UserPostMapper.class, CALLS_REAL_METHODS);

        doReturn(1).when(mapper).delete(any());

        List<Long> ids = multiple ? List.of(2L, 3L) : List.of(2L);

        assertThat(new UserPostGatewayImpl(mapper).deleteUserPostByUserIds(ids)).isTrue();

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

        assertThat(new UserRoleGatewayImpl(roles).deleteUserRoleByUserIds(ids)).isFalse();

        assertThat(new UserRoleGatewayImpl(roles).deleteUserRoleInfosByRoleId(10L, ids)).isFalse();

        assertThat(new UserPostGatewayImpl(posts).deleteUserPostByUserIds(ids)).isFalse();

        verify(roles, never()).delete(any());

        verify(posts, never()).delete(any());
    }

    private void assertUserIdBindings(LambdaQueryWrapper<?> wrapper, List<Long> ids) {
        assertThat(wrapper.getSqlSegment()).contains("user_id IN");

        assertThat(wrapper.getParamNameValuePairs().values())
            .containsExactlyInAnyOrderElementsOf(ids);
    }

    /**
     * 用户和角色编号不能因两个Long参数位置相反而撤销错误的授权。
     */
    @Test
    void singleRoleRevocationBindsUserAndRoleToTheirOwnColumns() {
        UserRoleMapper mapper = mock(UserRoleMapper.class);
        doReturn(1).when(mapper).delete(any());
        var gateway = new UserRoleGatewayImpl(mapper);
        assertThat(gateway.deleteUserRoleInfo(2L, 10L)).isTrue();
        ArgumentCaptor<LambdaQueryWrapper<UserRoleDO>> captor = ArgumentCaptor.captor();
        verify(mapper).delete(captor.capture());
        var query = captor.getValue();
        assertColumnBinding(query, "user_id", 2L);
        assertColumnBinding(query, "role_id", 10L);
    }

    private void assertColumnBinding(LambdaQueryWrapper<?> query, String column, Long value) {
        var binding =
            Pattern.compile(column + " = #\\{ew\\.paramNameValuePairs\\.([^}]+)}")
                .matcher(query.getSqlSegment());
        assertThat(binding.find()).isTrue();
        assertThat(query.getParamNameValuePairs().get(binding.group(1))).isEqualTo(value);
    }
}
