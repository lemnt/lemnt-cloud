package com.lemnt.product.infrastructure.product;

import com.lemnt.product.domain.product.model.StoreFilter;
import com.lemnt.product.infrastructure.product.gateway.ProductCategoryGatewayImpl;
import com.lemnt.product.infrastructure.product.mapper.ProductCategoryMapper;
import com.lemnt.product.infrastructure.product.mapper.ProductSpuMapper;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 使用真实Mapper XML执行过滤、分页、排序及字段映射；不替代目标MySQL验收。 */
class StoreCatalogQueryTest {
    @Test
    void realMapperSharesFiltersAndOrdersByPriceOrSalesWithStableTies() throws Exception {
        var source = new JdbcDataSource();
        source.setURL("jdbc:h2:mem:store_query;MODE=MySQL;DB_CLOSE_DELAY=-1");
        try (var connection = source.getConnection();
                var statement = connection.createStatement()) {
            statement.execute(
                    "CREATE TABLE product_category(id BIGINT PRIMARY KEY,parent_id BIGINT,name VARCHAR(50),pic_url VARCHAR(100),sort INT,status VARCHAR(20))");
            statement.execute(
                    "INSERT INTO product_category VALUES(1,NULL,'parent',NULL,9,'ENABLED'),(2,1,'child',NULL,1,'ENABLED'),(3,1,'hidden',NULL,0,'DISABLED'),(4,2,'grandchild',NULL,1,'ENABLED')");
            statement.execute(
                    "CREATE TABLE product_spu(id BIGINT PRIMARY KEY,code VARCHAR(20),name VARCHAR(50),keyword VARCHAR(30),introduction VARCHAR(50),slider_pic_urls VARCHAR(100),sort INT,category_id BIGINT,brand_id BIGINT,description VARCHAR(100),image VARCHAR(100),status VARCHAR(20),delivery_types VARCHAR(30),delivery_template_id BIGINT,sales_count INT,create_time TIMESTAMP,virtual_sales_count INT,spec_type BOOLEAN)");
            statement.execute(
                    "INSERT INTO product_spu VALUES(1,'A','goods',NULL,'intro','[\"https://a.test/p.png\"]',1,2,1,NULL,NULL,'ON_SHELF','[1]',8,5,NULL,NULL,NULL),(2,'B','goods',NULL,NULL,NULL,1,2,1,NULL,NULL,'ON_SHELF',NULL,NULL,20,TIMESTAMP '2025-01-01 01:00:00',2,FALSE),(3,'C','other',NULL,NULL,NULL,1,3,1,NULL,NULL,'ON_SHELF',NULL,NULL,30,TIMESTAMP '2025-02-01 01:00:00',0,TRUE),(4,'D','goods',NULL,NULL,NULL,1,2,1,NULL,NULL,'OFF_SHELF',NULL,NULL,40,NULL,NULL,NULL)");
            statement.execute("ALTER TABLE product_spu ADD give_integral INT");
            statement.execute("ALTER TABLE product_spu ADD sub_commission_type BOOLEAN");
            statement.execute("UPDATE product_spu SET give_integral=7 WHERE id=1");
            statement.execute(
                    "CREATE TABLE product_sku(id BIGINT PRIMARY KEY,spu_id BIGINT,price DECIMAL(18,2),stock INT,status VARCHAR(20))");
            statement.execute(
                    "INSERT INTO product_sku VALUES(11,1,10.00,0,'ON_SHELF'),(12,1,1.00,3,'OFF_SHELF'),(21,2,2.00,5,'ON_SHELF'),(31,3,1.00,5,'ON_SHELF'),(41,4,1.00,5,'ON_SHELF')");
            statement.execute("ALTER TABLE product_sku ADD market_price DECIMAL(18,2)");
            statement.execute("ALTER TABLE product_sku ADD cost_price DECIMAL(18,2)");
            statement.execute(
                    "UPDATE product_sku SET market_price=12.00,cost_price=0.00 WHERE id=11");
            statement.execute("UPDATE product_sku SET market_price=2.00 WHERE id=12");
            statement.execute("INSERT INTO product_sku VALUES(13,1,0.00,100,'DELETED',0.00,0.00)");
        }
        var factory = new MybatisSqlSessionFactoryBean();
        factory.setDataSource(source);
        var configuration = new MybatisConfiguration();
        configuration.addMapper(ProductSpuMapper.class);
        configuration.addMapper(ProductCategoryMapper.class);
        factory.setConfiguration(configuration);
        factory.setMapperLocations(
                new ClassPathResource("mapper/ProductSpuMapper.xml"),
                new ClassPathResource("mapper/ProductCategoryMapper.xml"));
        try (var session = factory.getObject().openSession()) {
            var mapper = session.getMapper(ProductSpuMapper.class);
            assertEquals(7, mapper.lockSpu(1L).getGiveIntegral());
            var totals = mapper.skuTotals(List.of(1L, 99L));
            assertEquals(1, totals.size());
            assertEquals(1L, totals.get(0).spuId());
            assertEquals(new BigDecimal("1.00"), totals.get(0).price());
            assertEquals(new BigDecimal("2.00"), totals.get(0).marketPrice());
            assertNull(totals.get(0).costPrice());
            assertEquals(3L, totals.get(0).stock());
            var filter = new StoreFilter("goods", null, 0, 1, List.of(2L), "price", true);
            assertEquals(2, mapper.storeCatalogCount(filter));
            assertEquals(2L, mapper.products(filter).get(0).getId());
            assertEquals(
                    1L,
                    mapper.products(
                                    new StoreFilter(
                                            "goods", null, 1, 1, List.of(2L), "price", true))
                            .get(0)
                            .getId());
            assertEquals(
                    1L,
                    mapper.products(new StoreFilter(null, null, 0, 20, List.of(2L), "price", false))
                            .get(0)
                            .getId());
            assertEquals(
                    2L,
                    mapper.products(
                                    new StoreFilter(
                                            null, null, 0, 20, List.of(2L), "salesCount", false))
                            .get(0)
                            .getId());
            assertEquals(
                    1L,
                    mapper.products(
                                    new StoreFilter(
                                            null, null, 0, 20, List.of(2L), "salesCount", true))
                            .get(0)
                            .getId());
            assertEquals(
                    0,
                    mapper.storeCatalogCount(
                            new StoreFilter(null, null, 0, 20, List.of(99L), null, true)));
            assertEquals(20, mapper.products(filter).get(0).getSalesCount());
            assertEquals(22L, mapper.products(filter).get(0).getDisplaySalesCount());
            assertNull(
                    mapper.products(
                                    new StoreFilter(
                                            "goods", null, 1, 1, List.of(2L), "price", true))
                            .get(0)
                            .getDisplaySalesCount());
            try (var statement = session.getConnection().createStatement()) {
                statement.execute("UPDATE product_sku SET price=10.00 WHERE id=21");
                statement.execute("UPDATE product_spu SET sales_count=5 WHERE id=2");
            }
            assertEquals(
                    List.of(2L, 1L),
                    mapper
                            .products(
                                    new StoreFilter(null, null, 0, 20, List.of(2L), "price", true))
                            .stream()
                            .map(v -> v.getId())
                            .toList());
            assertEquals(
                    List.of(2L, 1L),
                    mapper
                            .products(
                                    new StoreFilter(
                                            null, null, 0, 20, List.of(2L), "salesCount", false))
                            .stream()
                            .map(v -> v.getId())
                            .toList());
            assertEquals(
                    List.of(3L, 2L, 1L),
                    mapper
                            .products(
                                    new StoreFilter(
                                            null, null, 0, 20, List.of(), "createTime", false))
                            .stream()
                            .map(v -> v.getId())
                            .toList());
            assertEquals(
                    List.of(2L, 3L, 1L),
                    mapper
                            .products(
                                    new StoreFilter(
                                            null, null, 0, 20, List.of(), "createTime", true))
                            .stream()
                            .map(v -> v.getId())
                            .toList());
            try (var statement = session.getConnection().createStatement()) {
                statement.execute("UPDATE product_sku SET price=0 WHERE id=21");
            }
            assertEquals(
                    2L,
                    mapper.products(new StoreFilter(null, null, 0, 20, List.of(2L), "price", true))
                            .get(0)
                            .getId());
            var row = mapper.lockSpu(1L);
            assertEquals("intro", row.getIntroduction());
            assertEquals("[\"https://a.test/p.png\"]", row.getSliderPicUrls());
            var gateway =
                    new ProductCategoryGatewayImpl(session.getMapper(ProductCategoryMapper.class));
            assertEquals(
                    List.of(2L, 4L, 1L),
                    gateway.listEnableCategories().stream().map(v -> v.id()).toList());
            assertEquals(
                    List.of(2L),
                    gateway.listEnableCategories(List.of(2L, 3L)).stream()
                            .map(v -> v.id())
                            .toList());
            assertTrue(gateway.listEnableCategories(List.of()).isEmpty());
            assertEquals(
                    List.of(2L),
                    gateway.enabledChildren(List.of(1L)).stream().map(v -> v.id()).toList());
        }
    }
}
