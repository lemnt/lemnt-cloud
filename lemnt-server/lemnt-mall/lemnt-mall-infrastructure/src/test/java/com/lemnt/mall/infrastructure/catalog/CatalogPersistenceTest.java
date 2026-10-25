package com.lemnt.mall.infrastructure.catalog;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import com.lemnt.mall.domain.catalog.gateway.CatalogGateway;
import com.lemnt.mall.domain.catalog.model.*;
import com.lemnt.mall.domain.gateway.ProductGateway;
import com.lemnt.mall.infrastructure.catalog.gateway.CatalogGatewayImpl;
import com.lemnt.mall.infrastructure.catalog.mapper.CatalogMapper;
import com.lemnt.mall.infrastructure.gateway.ProductGatewayImpl;
import com.lemnt.mall.infrastructure.mapper.*;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.AnnotationTransactionAttributeSource;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Production migration SQL and gateways with real local transaction proxies, H2 MySQL mode. */
class CatalogPersistenceTest {
    JdbcDataSource source;
    java.sql.Connection schemaConnection;
    CatalogGateway catalog;
    ProductGateway products;
    CatalogMapper mapper;
    Long categoryId;
    Long brandId;

    @BeforeEach
    void setup() throws Exception {
        source = new JdbcDataSource();
        source.setURL(
                "jdbc:h2:mem:catalog_"
                        + UUID.randomUUID()
                        + ";MODE=MySQL;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=5000");
        schemaConnection = source.getConnection();
        {
            ScriptUtils.executeSqlScript(
                    schemaConnection, new ClassPathResource("sql/V1__mall_product.sql"));
            ScriptUtils.executeSqlScript(
                    schemaConnection, new ClassPathResource("sql/V2__mall_catalog.sql"));
        }
        var config = new MybatisConfiguration();
        config.setMapUnderscoreToCamelCase(true);
        var bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(source);
        bean.setGlobalConfig(
                new com.baomidou.mybatisplus.core.config.GlobalConfig()
                        .setMetaObjectHandler(
                                new com.lemnt.common.mybatis.handler.MyMetaObjectHandler()));
        bean.setConfiguration(config);
        var factory = bean.getObject();
        config.addMapper(CatalogMapper.class);
        config.addMapper(ProductMapper.class);
        config.addMapper(StockReservationMapper.class);
        var template = new SqlSessionTemplate(factory);
        mapper = template.getMapper(CatalogMapper.class);
        catalog = transactional(new CatalogGatewayImpl(mapper), CatalogGateway.class);
        products =
                transactional(
                        new ProductGatewayImpl(
                                template.getMapper(ProductMapper.class),
                                template.getMapper(StockReservationMapper.class)),
                        ProductGateway.class);
        categoryId =
                catalog.saveCategory(new Category(null, null, "category", null, 0, "ENABLED")).id();
        brandId = catalog.saveBrand(new Brand(null, "brand", null, null, 0, "ENABLED")).id();
    }

    @org.junit.jupiter.api.AfterEach
    void closeSchemaConnection() throws Exception {
        // H2 2.4 keeps a session comparator in CHECK IN constant sets; retain the DDL session.
        if (schemaConnection != null) schemaConnection.close();
    }

    <T> T transactional(Object target, Class<T> type) {
        var advice =
                new TransactionInterceptor(
                        new DataSourceTransactionManager(source),
                        new AnnotationTransactionAttributeSource());
        var proxy = new ProxyFactory(target);
        proxy.addAdvice(advice);
        return type.cast(proxy.getProxy());
    }

    CatalogSku draft(String code, int stock) {
        return new CatalogSku(
                null,
                null,
                code,
                "sku",
                BigDecimal.TEN,
                stock,
                null,
                Map.of("颜色", code),
                "ON_SHELF");
    }

    CatalogSpu create(String code, CatalogSku... skus) {
        return catalog.createSpu(
                CatalogRules.newSpu(
                        new CatalogSpu(
                                null,
                                code,
                                "goods",
                                categoryId,
                                brandId,
                                "description",
                                null,
                                null,
                                List.of(skus))));
    }

    long count(String table) throws Exception {
        try (var connection = source.getConnection();
                var statement = connection.createStatement();
                var rows = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rows.next();
            return rows.getLong(1);
        }
    }

    @Test
    void legacyStandaloneProductKeepsItsAvailabilityAfterCatalogExtension() {
        var old =
                products.save(
                        com.lemnt.mall.domain.model.Product.create(
                                "OLD", "old", null, null, BigDecimal.ONE, 3));
        products.updateStatus(old.id(), "ON_SHELF");
        assertEquals(old.id(), products.findSellableById(old.id()).id());
        assertTrue(products.reserveStock("legacy", old.id(), 1));
    }

    @Test
    void additiveMigrationPreservesAnExistingProductRow() throws Exception {
        var oldSource = new JdbcDataSource();
        oldSource.setURL("jdbc:h2:mem:legacy_" + UUID.randomUUID() + ";MODE=MySQL");
        try (var connection = oldSource.getConnection();
                var statement = connection.createStatement()) {
            ScriptUtils.executeSqlScript(
                    connection, new ClassPathResource("sql/V1__mall_product.sql"));
            statement.execute(
                    "INSERT INTO mall_product(id,product_code,name,price,stock,status) "
                            + "VALUES(42,'LEGACY','legacy',9.25,7,'ON_SHELF')");
            ScriptUtils.executeSqlScript(
                    connection, new ClassPathResource("sql/V2__mall_catalog.sql"));
            try (var row =
                    statement.executeQuery(
                            "SELECT id,price,stock,spu_id FROM mall_product WHERE id=42")) {
                assertTrue(row.next());
                assertEquals(42, row.getLong("id"));
                assertEquals(new BigDecimal("9.25"), row.getBigDecimal("price"));
                assertEquals(7, row.getInt("stock"));
                assertNull(row.getObject("spu_id"));
            }
        }
    }

    @Test
    void disablingParentOrBrandAlsoBlocksChildSkuSales() {
        Long childId =
                catalog.saveCategory(new Category(null, categoryId, "child", null, 0, "ENABLED"))
                        .id();
        var draft =
                CatalogRules.newSpu(
                        new CatalogSpu(
                                null,
                                "SP1",
                                "goods",
                                childId,
                                brandId,
                                null,
                                null,
                                null,
                                List.of(draft("A", 5))));
        var spu = catalog.createSpu(draft);
        Long sku = spu.skus().get(0).id();
        catalog.shelf(spu.id(), true);
        catalog.saveCategory(new Category(categoryId, null, "root", null, 0, "DISABLED"));
        assertNull(products.findSellableById(sku));
        assertFalse(products.reserveStock("PARENT", sku, 1));
        catalog.saveCategory(new Category(categoryId, null, "root", null, 0, "ENABLED"));
        catalog.saveBrand(new Brand(brandId, "brand", null, null, 0, "DISABLED"));
        assertNull(products.findSellableById(sku));
        assertFalse(products.reserveStock("BRAND", sku, 1));
    }

    @Test
    void createsVariantsAndRollsBackWholeAggregateOnDuplicateSku() throws Exception {
        var first = create("SP1", draft("A", 4));
        assertEquals(Map.of("颜色", "A"), first.skus().get(0).properties());
        assertThrows(RuntimeException.class, () -> create("SP2", draft("B", 2), draft("A", 2)));
        assertEquals(1, count("mall_spu"));
        assertEquals(1, count("mall_product"));
        assertEquals(1, count("mall_sku_property"));
    }

    @Test
    void disabledTaxonomyAndOffShelfBlockNewReservationsButKeepExistingReservationRecoverable() {
        var spu = create("SP1", draft("A", 5));
        Long sku = spu.skus().get(0).id();
        assertNull(products.findSellableById(sku));
        assertFalse(products.reserveStock("O0", sku, 1));
        catalog.shelf(spu.id(), true);
        assertTrue(products.reserveStock("O1", sku, 2));
        assertTrue(products.reserveStock("O2", sku, 1));
        catalog.saveCategory(new Category(categoryId, null, "category", null, 0, "DISABLED"));
        assertNull(products.findSellableById(sku));
        assertFalse(products.reserveStock("O3", sku, 1));
        products.confirmStock("O1", sku);
        products.confirmStock("O1", sku);
        products.releaseStock("O2", sku);
        products.releaseStock("O2", sku);
        assertEquals(3, products.findById(sku).stock());
    }

    @Test
    void ledgerIsIdempotentRejectsChangedPayloadAndRollsBackFailedAdjustment() throws Exception {
        var spu = create("SP1", draft("A", 5));
        Long sku = spu.skus().get(0).id();
        var request = new StockAdjustment("R1", sku, 3, "receipt", null);
        assertEquals(8, catalog.adjustStock(request).resultingStock());
        assertEquals(8, catalog.adjustStock(request).resultingStock());
        assertThrows(
                IllegalStateException.class,
                () -> catalog.adjustStock(new StockAdjustment("R1", sku, 4, "receipt", null)));
        assertThrows(
                IllegalStateException.class,
                () -> catalog.adjustStock(new StockAdjustment("R2", sku, -9, "count", null)));
        assertEquals(1, count("mall_stock_adjustment"));
        assertEquals(8, products.findById(sku).stock());
        catalog.adjustStock(new StockAdjustment("R3", sku, Integer.MAX_VALUE - 8, "limit", null));
        assertThrows(
                IllegalStateException.class,
                () -> catalog.adjustStock(new StockAdjustment("R4", sku, 1, "overflow", null)));
    }

    @Test
    void adjustmentLeavesCapacityForOutstandingReservationRelease() {
        var spu = create("SP1", draft("A", 5));
        Long sku = spu.skus().get(0).id();
        catalog.shelf(spu.id(), true);
        assertTrue(products.reserveStock("O1", sku, 2));
        assertThrows(
                IllegalStateException.class,
                () ->
                        catalog.adjustStock(
                                new StockAdjustment(
                                        "MAX", sku, Integer.MAX_VALUE - 3, "receipt", null)));
        products.releaseStock("O1", sku);
        assertEquals(5, products.findById(sku).stock());
    }

    @Test
    void foreignKeysProtectReferencedTaxonomyIncludingOffShelfProducts() {
        create("SP1", draft("A", 5));
        assertThrows(IllegalStateException.class, () -> catalog.deleteCategory(categoryId));
        assertThrows(IllegalStateException.class, () -> catalog.deleteBrand(brandId));
    }

    @Test
    void concurrentStockDeltasDoNotLoseUpdatesOrAllowNegativeInventory() throws Exception {
        var spu = create("SP1", draft("A", 5));
        Long sku = spu.skus().get(0).id();
        var executor = Executors.newFixedThreadPool(8);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int i = 0; i < 12; i++) {
                final int index = i;
                tasks.add(
                        () -> {
                            try {
                                catalog.adjustStock(
                                        new StockAdjustment("C" + index, sku, -1, "count", null));
                                return true;
                            } catch (IllegalStateException exhausted) {
                                return false;
                            }
                        });
            }
            long successes = 0;
            for (var future : executor.invokeAll(tasks)) if (future.get()) successes++;
            assertEquals(5, successes);
            assertEquals(0, products.findById(sku).stock());
            assertEquals(5, count("mall_stock_adjustment"));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void pagingAndMetadataChangesNeverReplaceSkuInventory() {
        var spu = create("SP1", draft("A", 5));
        assertEquals(1, catalog.page(new CatalogFilter("goods", null, 0, 10)).total());
        assertTrue(
                catalog.updateSpu(
                        new CatalogSpu(
                                spu.id(),
                                null,
                                "updated",
                                categoryId,
                                brandId,
                                null,
                                null,
                                null,
                                List.of())));
        assertEquals(5, catalog.spu(spu.id()).skus().get(0).stock());
        assertTrue(catalog.page(new CatalogFilter("missing", null, 0, 10)).records().isEmpty());
    }
}
