package com.lemnt.system.infrastructure.auth.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/** 名称查询限定同一父菜单，修改时在查询中排除自身。 */
class MenuNameUniqueMapperTest {
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void scopesNameToParentAndExcludesSelfWhenEditing(boolean editing) throws Exception {
        Configuration configuration = new Configuration();
        String resource = "mapper/MenuMapper.xml";
        try (var input = Resources.getResourceAsStream(resource)) {
            new XMLMapperBuilder(input, configuration, resource, configuration.getSqlFragments()).parse();
        }
        var parameters = new HashMap<String, Object>();
        parameters.put("menuName", "测试菜单");
        parameters.put("parentId", 0L);
        parameters.put("excludeMenuId", editing ? 2L : null);
        var boundSql = configuration.getMappedStatement(MenuMapper.class.getName() + ".checkMenuNameUnique")
                .getBoundSql(parameters);
        String sql = boundSql.getSql().replaceAll("\\s+", " ");
        assertThat(sql).contains("menu_name = ? AND parent_id = ?");
        if (editing) {
            assertThat(sql).contains("menu_id != ?");
            assertThat(boundSql.getParameterMappings()).extracting("property")
                    .containsExactly("menuName", "parentId", "excludeMenuId");
        } else {
            assertThat(sql).doesNotContain("menu_id !=");
            assertThat(boundSql.getParameterMappings()).extracting("property")
                    .containsExactly("menuName", "parentId");
        }
    }
}
