package com.lemnt.mall.infrastructure.catalog;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CatalogMenuSeedTest {
    @Test
    void menuSeedIsRepeatableAndCreatesNoRoleGrants() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:menus_" + UUID.randomUUID() + ";MODE=MySQL");
        try (var connection = source.getConnection();
                var statement = connection.createStatement()) {
            // Contract fixture for the columns used from the existing sys_menu schema.
            statement.execute(
                    "CREATE TABLE sys_menu(menu_id BIGINT AUTO_INCREMENT PRIMARY KEY, menu_name"
                    + " VARCHAR(50),parent_id BIGINT,order_num INT,path VARCHAR(200),component"
                    + " VARCHAR(255),is_frame INT,is_cache INT,menu_type CHAR(1),visible"
                    + " CHAR(1),status CHAR(1),perms VARCHAR(100),icon VARCHAR(100),create_by"
                    + " VARCHAR(64),create_time TIMESTAMP)");
            var script = new ClassPathResource("sql/V20261025__mall_catalog_menu.sql");
            ScriptUtils.executeSqlScript(connection, script);
            ScriptUtils.executeSqlScript(connection, script);
            try (var rows =
                    statement.executeQuery("SELECT COUNT(*) FROM sys_menu WHERE menu_type='C'")) {
                rows.next();
                assertEquals(3, rows.getInt(1));
            }
            try (var rows =
                    statement.executeQuery(
                    "SELECT COUNT(*) FROM sys_menu WHERE perms='mall:sku:stock'")) {
                rows.next();
                assertEquals(1, rows.getInt(1));
            }
        }
    }
}
