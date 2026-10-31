package com.lemnt.trade.start.payment;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 支付派发任务菜单可重入，不覆盖既有菜单或擅自分配角色。 */
class PayNotifyMenuSeedTest {
    @Test
    void seedsPayNotifyPageIdempotentlyWithoutRoleChanges() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:pay_notify_menu;MODE=MySQL");
        try (var connection = source.getConnection(); var s = connection.createStatement()) {
            s.execute("CREATE TABLE sys_menu(menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,menu_name VARCHAR(100),parent_id BIGINT,order_num INT,path VARCHAR(200),component VARCHAR(200),is_frame INT,is_cache INT,menu_type CHAR(1),visible CHAR(1),status CHAR(1),perms VARCHAR(100),icon VARCHAR(100),create_by VARCHAR(64),create_time DATETIME)");
            s.execute("CREATE TABLE sys_role_menu(role_id BIGINT,menu_id BIGINT)");
            s.execute("INSERT INTO sys_menu(menu_name,parent_id,path,menu_type) VALUES('原交易名',0,'trade','M')");
            s.execute("INSERT INTO sys_role_menu VALUES(7,1)");
            var script = new ClassPathResource("setup-sql/V20261031__trade_pay_notify_menu.sql");
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, script);
            try (var r = s.executeQuery("SELECT COUNT(*) FROM sys_menu")) {
                r.next();
                assertEquals(4, r.getInt(1));
            }
            try (var r = s.executeQuery("SELECT COUNT(*) FROM sys_menu WHERE component='trade/pay-notify/index'")) {
                r.next();
                assertEquals(1, r.getInt(1));
            }
            try (var r = s.executeQuery("SELECT menu_name FROM sys_menu WHERE menu_id=1")) {
                r.next();
                assertEquals("原交易名", r.getString(1));
            }
            try (var r = s.executeQuery("SELECT COUNT(*) FROM sys_role_menu")) {
                r.next();
                assertEquals(1, r.getInt(1));
            }
        }
    }
}
