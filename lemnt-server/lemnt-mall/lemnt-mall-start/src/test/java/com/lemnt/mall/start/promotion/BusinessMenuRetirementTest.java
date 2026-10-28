package com.lemnt.mall.start.promotion;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 菜单收敛可重入，移除旧权限且保留仍使用的券和售后页面角色关联。 */
class BusinessMenuRetirementTest {
    @Test
    void retiresOnlyRemovedActionsAndPreservesRetainedBindings() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:retire_menu;MODE=MySQL");
        try (var connection = source.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE sys_menu(menu_id BIGINT PRIMARY KEY,parent_id BIGINT,"
                    + "menu_name VARCHAR(100),component VARCHAR(100),perms VARCHAR(100))");
            statement.execute("CREATE TABLE sys_role_menu(role_id BIGINT,menu_id BIGINT)");
            statement.execute("INSERT INTO sys_menu VALUES"
                    + "(1,0,'券','mall/coupon/index','mall:coupon-template:list'),"
                    + "(2,0,'购物车','mall/cart/index','mall:cart:query'),"
                    + "(3,2,'会员券',NULL,'mall:coupon:list'),"
                    + "(4,0,'退款','trade/refund/index','trade:refund:list'),"
                    + "(5,4,'旧退款',NULL,'trade:refund:request'),"
                    + "(6,0,'发布',NULL,'mall:promotion:publish'),(7,0,'后台关单',NULL,'trade:order:close')");
            statement.execute("INSERT INTO sys_role_menu VALUES(7,2),(7,3),(7,4),(7,5),(7,6),(7,7)");
            var script = new ClassPathResource("setup-sql/V20261028__retire_extra_business_menus.sql");
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, script);
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_menu")) {
                rows.next();
                assertEquals(3, rows.getInt(1));
            }
            try (var rows = statement.executeQuery("SELECT parent_id FROM sys_menu WHERE menu_id=3")) {
                rows.next();
                assertEquals(1, rows.getInt(1));
            }
            try (var rows = statement.executeQuery("SELECT perms FROM sys_menu WHERE menu_id=4")) {
                rows.next();
                assertEquals("trade:after-sale:list", rows.getString(1));
            }
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_role_menu WHERE role_id=7")) {
                rows.next();
                assertEquals(2, rows.getInt(1));
            }
        }
    }
}
