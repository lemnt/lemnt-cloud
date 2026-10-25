package com.lemnt.member.start;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class MemberMenuSeedTest {
    @Test
    void repeatedSeedCreatesSixMemberPagesWithoutRoleWrites() throws Exception {
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
            var script = new ClassPathResource("setup-sql/V20261025__member_menu.sql");
            ScriptUtils.executeSqlScript(c, script);
            ScriptUtils.executeSqlScript(c, script);
            try (var r = s.executeQuery("SELECT COUNT(*) FROM sys_menu WHERE menu_type='C'")) {
                r.next();
                assertEquals(6, r.getInt(1));
            }
            try (var r =
                    s.executeQuery(
                            "SELECT COUNT(*) FROM sys_menu WHERE component='member/user/index'")) {
                r.next();
                assertEquals(1, r.getInt(1));
            }
            try (var r =
                    s.executeQuery(
                            "SELECT COUNT(*) FROM sys_menu WHERE"
                                + " perms='member:user:update-point'")) {
                r.next();
                assertEquals(1, r.getInt(1));
            }
            try (var r =
                    s.executeQuery(
                            "SELECT COUNT(*) FROM sys_menu WHERE perms='member:signin:query'")) {
                r.next();
                assertEquals(1, r.getInt(1));
            }
        }
    }
}
