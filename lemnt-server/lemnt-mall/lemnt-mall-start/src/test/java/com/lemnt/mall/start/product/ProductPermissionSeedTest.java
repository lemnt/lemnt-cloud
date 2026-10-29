package com.lemnt.mall.start.product;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 商品管理权限脚本的幂等及授权边界验证。 */
class ProductPermissionSeedTest {
    @Test
    void seedRequiresExistingRootAndNeverCreatesPagesOrGrantsRoles() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:product_menu_" + UUID.randomUUID() + ";MODE=MySQL");
        try (var connection = source.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE sys_menu(menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "menu_name VARCHAR(50),parent_id BIGINT,order_num INT,path VARCHAR(200),"
                    + "component VARCHAR(255),is_frame INT,is_cache INT,menu_type CHAR(1),"
                    + "visible CHAR(1),status CHAR(1),perms VARCHAR(100),icon VARCHAR(100),"
                    + "create_by VARCHAR(64),create_time TIMESTAMP)");
            statement.execute("CREATE TABLE sys_role_menu(role_id BIGINT,menu_id BIGINT)");
            var script = new ClassPathResource("setup-sql/V20261027__product_management_permissions.sql");
            var interactionScript = new ClassPathResource("setup-sql/V20261029__product_interaction_permissions.sql");
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, interactionScript);
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_menu")) {
                rows.next();
                assertEquals(0, rows.getInt(1));
            }
            statement.execute("INSERT INTO sys_menu(menu_id,parent_id,path,menu_type) VALUES(1,0,'mall','M')");
            statement.execute("INSERT INTO sys_role_menu VALUES(7,1)");
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, interactionScript);
            ScriptUtils.executeSqlScript(connection, interactionScript);
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_menu WHERE menu_type='F'")) {
                rows.next();
                assertEquals(22, rows.getInt(1));
            }
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_menu WHERE menu_type='C'")) {
                rows.next();
                assertEquals(0, rows.getInt(1));
            }
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_role_menu WHERE role_id=7 AND menu_id=1")) {
                rows.next();
                assertEquals(1, rows.getInt(1));
            }
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_role_menu")) {
                rows.next();
                assertEquals(1, rows.getInt(1));
            }
        }
    }
}
