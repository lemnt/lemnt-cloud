package com.lemnt.trade.start.checkout;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutMenuSeedTest {
    @Test
    void repeatedSeedsCreateFourPagesAndUniquePermissionsWithoutRoleWrites() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:trade_menu_" + UUID.randomUUID() + ";MODE=MySQL");
        try (var c = source.getConnection();
                var s = c.createStatement()) {
            s.execute(
                    "CREATE TABLE sys_menu(menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,menu_name"
                    + " VARCHAR(50),parent_id BIGINT,order_num INT,path VARCHAR(200),component"
                    + " VARCHAR(255),is_frame INT,is_cache INT,menu_type CHAR(1),visible"
                    + " CHAR(1),status CHAR(1),perms VARCHAR(100),icon VARCHAR(100),create_by"
                    + " VARCHAR(64),create_time TIMESTAMP)");
            var script = new ClassPathResource("setup-sql/V20261025__mall_checkout_menu.sql");
            ScriptUtils.executeSqlScript(c, script);
            ScriptUtils.executeSqlScript(c, script);
            ScriptUtils.executeSqlScript(
                    c, new ClassPathResource("setup-sql/V20261025__trade_fulfillment_menu.sql"));
            ScriptUtils.executeSqlScript(
                    c, new ClassPathResource("setup-sql/V20261025__trade_fulfillment_menu.sql"));
            var paymentPermissions = new ClassPathResource(
                    "setup-sql/V20261027__trade_payment_management_permissions.sql");
            ScriptUtils.executeSqlScript(c, paymentPermissions);
            ScriptUtils.executeSqlScript(c, paymentPermissions);
            try (var rows = s.executeQuery("SELECT COUNT(*) FROM sys_menu WHERE perms LIKE 'trade:payment%'")) {
                rows.next();
                assertEquals(6, rows.getInt(1));
            }
            var fulfillmentPermissions = new ClassPathResource(
                    "setup-sql/V20261027__trade_fulfillment_management_permissions.sql");
            ScriptUtils.executeSqlScript(c, fulfillmentPermissions);
            ScriptUtils.executeSqlScript(c, fulfillmentPermissions);
            try (var rows = s.executeQuery("SELECT COUNT(*) FROM sys_menu WHERE perms LIKE 'trade:after-sale:%'")) {
                rows.next();
                assertEquals(9, rows.getInt(1));
            }
            try (var r = s.executeQuery("SELECT COUNT(*) FROM sys_menu WHERE menu_type='C'")) {
                r.next();
                assertEquals(4, r.getInt(1));
            }
            try (var r =
                    s.executeQuery(
                    "SELECT COUNT(*) FROM sys_menu WHERE perms='mall:checkout:create'")) {
                r.next();
                assertEquals(1, r.getInt(1));
            }
            try (var r =
                    s.executeQuery(
                    "SELECT COUNT(*) FROM sys_menu WHERE perms='trade:fulfillment:ship'")) {
                r.next();
                assertEquals(1, r.getInt(1));
            }
            try (var r =
                    s.executeQuery(
                    "SELECT COUNT(*) FROM sys_menu WHERE perms='trade:refund:reject'")) {
                r.next();
                assertEquals(1, r.getInt(1));
            }
        }
    }

    /** 迁移原菜单的归属与权限，不更换菜单主键或改写角色关联。 */
    @Test
    void ownershipUpgradePreservesMenuAndRoleBindings() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:ownership_menu_" + UUID.randomUUID() + ";MODE=MySQL");
        try (var connection = source.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE sys_menu(menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,"
                    + "menu_name VARCHAR(50),parent_id BIGINT,order_num INT,path VARCHAR(200),"
                    + "component VARCHAR(255),is_frame INT,is_cache INT,menu_type CHAR(1),"
                    + "visible CHAR(1),status CHAR(1),perms VARCHAR(100),icon VARCHAR(100),"
                    + "create_by VARCHAR(64),create_time TIMESTAMP)");
            statement.execute("CREATE TABLE sys_role_menu(role_id BIGINT,menu_id BIGINT)");
            statement.execute("INSERT INTO sys_menu(menu_id,menu_name,parent_id,path,component,menu_type,perms)"
                    + " VALUES(1,'交易管理',0,'trade',NULL,'M',''),"
                    + "(10,'购物车',1,'cart','trade/cart/index','C','trade:cart:query'),"
                    + "(11,'编辑',10,'',NULL,'F','trade:cart:edit'),"
                    + "(12,'促销',1,'',NULL,'F','trade:promotion:create'),"
                    + "(13,'提单',10,'',NULL,'F','trade:order:create')");
            statement.execute("INSERT INTO sys_role_menu VALUES(7,10),(7,11),(7,12),(7,13)");
            var script = new ClassPathResource("setup-sql/V20261027__mall_shopping_ownership.sql");
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, script);
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_role_menu WHERE role_id=7"
                    + " AND menu_id IN (10,11,12,13)")) {
                rows.next();
                assertEquals(4, rows.getInt(1));
            }
            try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sys_menu WHERE menu_id IN (10,11,12,13)"
                    + " AND perms LIKE 'mall:%'")) {
                rows.next();
                assertEquals(4, rows.getInt(1));
            }
            try (var rows = statement.executeQuery("SELECT component FROM sys_menu WHERE menu_id=10")) {
                rows.next();
                assertEquals("mall/cart/index", rows.getString(1));
            }
        }
    }
}
