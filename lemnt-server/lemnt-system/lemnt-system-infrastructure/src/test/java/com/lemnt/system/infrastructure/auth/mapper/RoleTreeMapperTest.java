package com.lemnt.system.infrastructure.auth.mapper;

import com.lemnt.system.domain.auth.model.Role;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTreeMapperTest {

    @Test
    void shouldAlwaysExcludeSelectedParentsFromMenuAndDepartmentCheckedKeys() throws IOException {
        for (String name : new String[]{"Menu", "Dept"}) {
            var configuration = mapper(name);
            String namespace = name.equals("Menu")
                    ? "com.lemnt.system.infrastructure.auth.mapper.MenuMapper"
                    : "com.lemnt.system.infrastructure.org.mapper.DeptMapper";
            var sql = configuration.getMappedStatement(namespace + ".select" + name + "ListByRoleId")
                    .getBoundSql(Map.of("roleId", 4L));
            assertThat(sql.getSql()).contains("NOT IN", "parent_id");
            assertThat(sql.getParameterMappings()).hasSize(2)
                    .allSatisfy(parameter -> assertThat(parameter.getProperty()).isEqualTo("roleId"));
        }
    }

    @Test
    void shouldReadRolesWithoutRemovedColumns() throws IOException {
        var configuration = mapper("Role");
        var sql = configuration.getMappedStatement("com.lemnt.system.infrastructure.auth.mapper.RoleMapper.selectRoleList")
                .getBoundSql(Map.of("qry", new Role()));
        assertThat(sql.getSql()).contains("r.role_id", "r.data_scope")
                .doesNotContain("check_strictly");
    }

    @Test
    void shouldLoadOnlyEnabledMenusAndPublishedNoticesUsingStatusOne() throws IOException {
        var menus = mapper("Menu");
        var menuSql = menus.getMappedStatement("com.lemnt.system.infrastructure.auth.mapper.MenuMapper.selectMenuTreeByUserId")
                .getBoundSql(Map.of("userId", 4L)).getSql();
        assertThat(menuSql).contains("m.status = '1'", "ro.status = '1'");
        var notices = mapper("NoticeRead");
        var noticeSql = notices.getMappedStatement("com.lemnt.system.infrastructure.notice.mapper.NoticeReadMapper.selectUnreadCount")
                .getBoundSql(Map.of("userId", 4L)).getSql();
        assertThat(noticeSql).contains("n.status = '1'");
    }

    private Configuration mapper(String name) throws IOException {
        var configuration = new Configuration();
        String resource = "mapper/" + name + "Mapper.xml";
        try (var input = getClass().getClassLoader().getResourceAsStream(resource)) {
            assertThat(input).isNotNull();
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        return configuration;
    }
}
