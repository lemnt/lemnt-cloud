package com.lemnt.trade.start.delivery;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.sql.Connection;
import java.util.Set;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 配送与配置菜单可重入，不覆盖既有菜单或擅自分配角色。 */
class DeliveryConfigMenuSeedTest {

    @Test
    void seedsDeliveryAndConfigPagesIdempotentlyWithoutRoleChanges() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:delivery_config_menu;MODE=MySQL");
        try (var connection = source.getConnection(); var statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE sys_menu(
                        menu_id BIGINT AUTO_INCREMENT PRIMARY KEY, menu_name VARCHAR(100),
                        parent_id BIGINT, order_num INT, path VARCHAR(200), component VARCHAR(200),
                        is_frame INT, is_cache INT, menu_type CHAR(1), visible CHAR(1), status CHAR(1),
                        perms VARCHAR(100), icon VARCHAR(100), create_by VARCHAR(64), create_time DATETIME)
                    """);
            statement.execute("CREATE TABLE sys_role_menu(role_id BIGINT,menu_id BIGINT)");
            statement.execute("""
                    INSERT INTO sys_menu(menu_name,parent_id,path,menu_type)
                    VALUES('原交易名',0,'trade','M')
                    """);
            statement.execute("INSERT INTO sys_role_menu VALUES(7,1)");
            var script = new ClassPathResource("setup-sql/V20261031__trade_delivery_config_menu.sql");
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, script);

            assertEquals(13, count(connection, "SELECT COUNT(*) FROM sys_menu"));
            assertEquals(3, count(connection, """
                    SELECT COUNT(*) FROM sys_menu WHERE parent_id=1 AND component IN (
                        'trade/delivery/express/index', 'trade/delivery/express-template/index',
                        'trade/config/index')
                    """));
            try (var result = statement.executeQuery("SELECT menu_name FROM sys_menu WHERE menu_id=1")) {
                result.next();
                assertEquals("原交易名", result.getString(1));
            }
            try (var result = statement.executeQuery("SELECT role_id,menu_id FROM sys_role_menu")) {
                result.next();
                assertEquals(7, result.getLong(1));
                assertEquals(1, result.getLong(2));
            }
            assertEquals(1, count(connection, "SELECT COUNT(*) FROM sys_role_menu"));
            var actual = new HashSet<String>();
            try (var result = statement.executeQuery("SELECT perms FROM sys_menu WHERE perms IS NOT NULL AND perms<>''")) {
                while (result.next()) {
                    actual.add(result.getString(1));
                }
            }
            assertEquals(Set.of(
                    "trade:delivery-express:list", "trade:delivery-express:query",
                    "trade:delivery-express:create", "trade:delivery-express:update", "trade:delivery-express:delete",
                    "trade:delivery-template:list", "trade:delivery-template:query",
                    "trade:delivery-template:create", "trade:delivery-template:update", "trade:delivery-template:delete",
                    "trade:config:query", "trade:config:update"), actual);
        }
    }

    /** 读取菜单或授权行数，验证重复执行不会追加记录。 */
    private int count(Connection connection, String sql) throws Exception {
        try (var statement = connection.createStatement(); var result = statement.executeQuery(sql)) {
            result.next();
            return result.getInt(1);
        }
    }
}
