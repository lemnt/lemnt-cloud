package com.lemnt.mall.start.promotion;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** 砍价菜单可重入且不授予角色。 */
class BargainMenuSeedTest {
    @Test
    void seedsBothPagesIdempotentlyWithoutRoleChanges() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:bargain_menu_" + UUID.randomUUID() + ";MODE=MySQL");
        try (var connection = source.getConnection(); var s = connection.createStatement()) {
            s.execute("CREATE TABLE sys_menu(menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,menu_name VARCHAR(100),parent_id BIGINT,order_num INT,path VARCHAR(200),component VARCHAR(200),is_frame INT,is_cache INT,menu_type CHAR(1),visible CHAR(1),status CHAR(1),perms VARCHAR(100),icon VARCHAR(100),create_by VARCHAR(64),create_time DATETIME)");
            s.execute("CREATE TABLE sys_role_menu(role_id BIGINT,menu_id BIGINT)");
            var script = new ClassPathResource("setup-sql/V20261031__mall_bargain_menu.sql");
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, script);
            try (var r = s.executeQuery("SELECT COUNT(*) FROM sys_menu")) { r.next(); assertEquals(9, r.getInt(1)); }
            try (var r = s.executeQuery("SELECT COUNT(*) FROM sys_menu WHERE component IN ('mall/promotion/bargain-activity/index','mall/promotion/bargain-record/index')")) { r.next(); assertEquals(2, r.getInt(1)); }
            try (var r = s.executeQuery("SELECT COUNT(*) FROM sys_role_menu")) { r.next(); assertEquals(0, r.getInt(1)); }
        }
    }
}
