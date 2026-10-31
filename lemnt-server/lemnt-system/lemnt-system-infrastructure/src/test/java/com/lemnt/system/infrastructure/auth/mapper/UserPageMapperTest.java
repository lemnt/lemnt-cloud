package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.system.domain.auth.model.User;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;

import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.Map;
import javax.sql.rowset.RowSetMetaDataImpl;
import javax.sql.rowset.RowSetProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.delegatesTo;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserPageMapperTest {

    @Test
    void shouldSelectDepartmentNameWithoutChangingUserOrScopeFilters() throws Exception {
        var statement = mapper().getMappedStatement(UserMapper.class.getName() + ".selectPage");

        var query = new User().setDeptId(103L).setUserName("test").setDataScope(" AND u.user_id = 2");

        String sql = statement.getBoundSql(Map.of("qry", query)).getSql();

        assertThat(sql).contains("d.dept_name", "LEFT JOIN sys_dept d", "u.del_flag = '0'",
                "u.user_name LIKE", "FIND_IN_SET").doesNotContain("AND u.user_id = 2");

    }

    @Test
    void shouldMapJoinedDepartmentAndKeepMissingDepartmentNull() throws Exception {
        var configuration = mapper();

        var mapped = configuration.getMappedStatement(UserMapper.class.getName() + ".selectPage");

        var boundSql = mapped.getBoundSql(Map.of("qry", new User()));

        var metadata = new RowSetMetaDataImpl();

        String[] columns = {
            "user_id", "dept_id", "joined_dept_id", "dept_name"
                };

        metadata.setColumnCount(columns.length);

        for (int index = 0; index < columns.length; index++) {
            metadata.setColumnName(index + 1, columns[index]);

            metadata.setColumnLabel(index + 1, columns[index]);

            metadata.setColumnType(index + 1, index == 3 ? Types.VARCHAR : Types.BIGINT);

        }
        // 用离线结果集复现已关联部门及部门缺失两种情况，不连接真实用户库。
        try (var rows = RowSetProvider.newFactory().createCachedRowSet()) {
            rows.setMetaData(metadata);

            for (long userId : new long[]{
                    2L, 3L
                    }) {
                rows.moveToInsertRow();

                rows.updateLong(1, userId);

                rows.updateLong(2, 103L);

                if (userId == 2L) {
                    rows.updateLong(3, 103L);

                    rows.updateString(4, "测试部门");

                } else {
                    rows.updateNull(3);

                    rows.updateNull(4);

                }
                rows.insertRow();

                rows.moveToCurrentRow();

            }
            rows.beforeFirst();

            var resultSet = mock(ResultSet.class, delegatesTo(rows));

            doReturn(false).when(resultSet).isClosed();

            var statement = mock(Statement.class);

            when(statement.getResultSet()).thenReturn(resultSet);

            when(statement.getUpdateCount()).thenReturn(-1);

            var handler = new DefaultResultSetHandler(null, mapped, null, null, boundSql, RowBounds.DEFAULT);

            var users = handler.handleResultSets(statement);

            assertThat(users).hasSize(2).allSatisfy(user -> assertThat(user).isInstanceOf(User.class));

            var linked = users.stream().map(User.class::cast)
                    .filter(user -> user.getUserId() == 2L).findFirst().orElseThrow();

            assertThat(linked.getDept().getDeptId()).isEqualTo(103L);

            assertThat(linked.getDept().getDeptName()).isEqualTo("测试部门");

            var missing = users.stream().map(User.class::cast)
                    .filter(user -> user.getUserId() == 3L).findFirst().orElseThrow();

            assertThat(missing.getDeptId()).isEqualTo(103L);

            assertThat(missing.getDept()).isNull();

        }
    }

    private Configuration mapper() throws Exception {
        var configuration = new Configuration();

        String resource = "mapper/UserMapper.xml";

        try (var input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertThat(input).isNotNull();

            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();

        }
        return configuration;

    }
}
