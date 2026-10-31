package com.lemnt.mall.start.promotion;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PromotionPermissionSeedTest {
    @Test
    void seedIsRepeatableHasNoPageAndNeverGrantsRoles() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:promo_menu_" + UUID.randomUUID() + ";MODE=MySQL");
        try (var c = source.getConnection();
                var st = c.createStatement()) {
            st.execute(
                    "CREATE TABLE sys_menu(menu_id BIGINT AUTO_INCREMENT PRIMARY KEY,menu_name"
                    + " VARCHAR(50),parent_id BIGINT,order_num INT,path VARCHAR(200),component"
                    + " VARCHAR(255),is_frame INT,is_cache INT,menu_type CHAR(1),visible"
                    + " CHAR(1),status CHAR(1),perms VARCHAR(100),icon VARCHAR(100),create_by"
                    + " VARCHAR(64),create_time TIMESTAMP)");
            st.execute("CREATE TABLE sys_role_menu(role_id BIGINT,menu_id BIGINT)");
            var script =
            new ClassPathResource("setup-sql/V20261026__mall_promotion_permissions.sql");
            ScriptUtils.executeSqlScript(c, script);
            ScriptUtils.executeSqlScript(c, script);
            for (var expectation :
                    Map.of(
                    "SELECT COUNT(*) FROM sys_menu WHERE perms LIKE"
                    + " 'mall:promotion:%'",
                    5,
                    "SELECT COUNT(*) FROM sys_menu WHERE menu_type='C'",
                    0,
                    "SELECT COUNT(*) FROM sys_role_menu",
                    0,
                    "SELECT COUNT(*) FROM sys_menu WHERE menu_type='M'",
                    1)
                    .entrySet()) {
                try (var rows = st.executeQuery(expectation.getKey())) {
                    rows.next();
                    assertEquals(expectation.getValue(), rows.getInt(1));
                }
            }
        }
    }
}
