package com.lemnt.mall.start.product;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import static org.junit.jupiter.api.Assertions.*;

/** 最终建表包含经营字段；初始化不补造历史经营内容，默认排序0。 */
class SpuOperatingSchemaTest {
    @Test
    void finalSchemaKeepsHistoricalProductValuesWithoutInventingContent() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:spu_operating_upgrade;MODE=MySQL");
        try (var connection = source.getConnection();
                var statement = connection.createStatement()) {
            var script = new ClassPathResource("mall-sql/lemnt_mall.sql");
            ScriptUtils.executeSqlScript(connection, script);
            statement.execute("INSERT INTO mall_product_category(id,name) VALUES(1,'分类')");
            statement.execute("INSERT INTO mall_product_brand(id,name) VALUES(1,'品牌')");
            statement.execute(
                    "INSERT INTO mall_product_spu(id,code,name,category_id,brand_id) VALUES(7,'LEGACY','原商品',1,1)");
            ScriptUtils.executeSqlScript(connection, script);
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
