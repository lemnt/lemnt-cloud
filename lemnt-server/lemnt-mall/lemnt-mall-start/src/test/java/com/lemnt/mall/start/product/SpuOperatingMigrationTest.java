package com.lemnt.mall.start.product;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import static org.junit.jupiter.api.Assertions.*;

/** 旧库升级不补造经营内容，默认排序0保留原编号倒序语义。 */
class SpuOperatingMigrationTest {
    @Test
    void addsFieldsWithoutChangingHistoricalProductData() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:spu_operating_upgrade;MODE=MySQL");
        try (var connection = source.getConnection(); var statement = connection.createStatement()) {
            statement.execute("CREATE TABLE mall_product_spu(id BIGINT PRIMARY KEY, code VARCHAR(64),name VARCHAR(200))");
            statement.execute("INSERT INTO mall_product_spu VALUES(7,'LEGACY','原商品')");
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("sql/upgrade/spu_operating_fields.sql"));
            try (var rows = statement.executeQuery("SELECT * FROM mall_product_spu")) {
                rows.next();
                assertEquals(7, rows.getLong("id"));
                assertEquals("LEGACY", rows.getString("code"));
                assertEquals("原商品", rows.getString("name"));
                assertNull(rows.getString("keyword"));
                assertNull(rows.getString("introduction"));
                assertNull(rows.getString("slider_pic_urls"));
                assertEquals(0, rows.getInt("sort"));
                assertFalse(rows.next());
            }
        }
    }
}
