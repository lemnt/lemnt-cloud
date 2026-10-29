package com.lemnt.mall.start.product;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import static org.junit.jupiter.api.Assertions.assertEquals;

/** 文章菜单脚本重复执行不增副本，不自动授权角色。 */
class ArticlePermissionSeedTest {
    @Test
    void menusRequireMallRootAndAreIdempotentWithoutRoleGrants() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:article_menu;MODE=MySQL");
        try (var connection = source.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE sys_menu(menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,menu_name VARCHAR(64),parent_id BIGINT,order_num INT,path VARCHAR(200),component VARCHAR(255),is_frame INT,is_cache INT,menu_type CHAR(1),visible CHAR(1),status CHAR(1),perms VARCHAR(100),icon VARCHAR(100),create_by VARCHAR(64),create_time TIMESTAMP)");
            statement.execute("CREATE TABLE sys_role_menu(role_id BIGINT,menu_id BIGINT)");
            var script = new ClassPathResource("setup-sql/V20261029__article_menu.sql");
            ScriptUtils.executeSqlScript(connection, script);
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_menu")) {
                rows.next(); assertEquals(0, rows.getInt(1));
            }
            statement.execute("INSERT INTO sys_menu(parent_id,path,menu_type) VALUES(0,'mall','M')");
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, script);
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_menu WHERE menu_type='C'")) {
                rows.next(); assertEquals(2, rows.getInt(1));
            }
            try (var rows = statement.executeQuery("SELECT COUNT(DISTINCT perms) FROM sys_menu WHERE perms IS NOT NULL")) {
                rows.next(); assertEquals(10, rows.getInt(1));
            }
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_role_menu")) {
                rows.next(); assertEquals(0, rows.getInt(1));
            }
        }
    }
}
