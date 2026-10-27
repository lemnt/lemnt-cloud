package com.lemnt.mall.start.catalog;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.mall.application.catalog.service.CatalogService;
import com.lemnt.mall.application.product.service.ProductInventoryService;
import com.lemnt.mall.domain.catalog.gateway.BrandGateway;
import com.lemnt.mall.domain.catalog.gateway.CategoryGateway;
import com.lemnt.mall.domain.catalog.gateway.SpuGateway;
import com.lemnt.mall.domain.catalog.model.Brand;
import com.lemnt.mall.domain.catalog.model.CatalogFilter;
import com.lemnt.mall.domain.catalog.model.CatalogRules;
import com.lemnt.mall.domain.catalog.model.CatalogSku;
import com.lemnt.mall.domain.catalog.model.CatalogSpu;
import com.lemnt.mall.domain.catalog.model.Category;
import com.lemnt.mall.domain.catalog.model.StockAdjustment;
import com.lemnt.mall.domain.product.gateway.ProductGateway;
import com.lemnt.mall.domain.product.model.Product;
import com.lemnt.mall.domain.store.model.StoreFilter;
import com.lemnt.mall.start.TableTestComponents;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.spring.MybatisSqlSessionFactoryBean;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.AfterEach;
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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Production migration SQL and gateways with real local transaction proxies, H2 MySQL mode.
 */
class CatalogPersistenceTest {
    TableTestComponents components;
    CategoryGateway categoryGateway;
    BrandGateway brandGateway;
    SpuGateway spuGateway;
    ProductGateway productGateway;
    CatalogService catalogService;
    ProductInventoryService productInventoryService;

    JdbcDataSource source;
    Connection schemaConnection;
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
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        config.addInterceptor(interceptor);
        config.setMapUnderscoreToCamelCase(true);
        var bean = new MybatisSqlSessionFactoryBean();
        bean.setDataSource(source);
        bean.setGlobalConfig(
                new GlobalConfig()
                .setMetaObjectHandler(
                new MyMetaObjectHandler()));
        bean.setConfiguration(config);
        var factory = bean.getObject();
        config.addMappers("com.lemnt.mall.infrastructure");

        var template = new SqlSessionTemplate(factory);
        components = new TableTestComponents(template, source);
        categoryGateway = components.get(CategoryGateway.class);
        brandGateway = components.get(BrandGateway.class);
        spuGateway = components.get(SpuGateway.class);
        productGateway = components.get(ProductGateway.class);
        catalogService = components.get(CatalogService.class);
        productInventoryService = components.get(ProductInventoryService.class);

        categoryId =
        categoryGateway.saveCategory(new Category(null, null, "category", null, 0, "ENABLED")).id();
        brandId = brandGateway.saveBrand(new Brand(null, "brand", null, null, 0, "ENABLED")).id();
    }

    @AfterEach
    void closeSchemaConnection() throws Exception {
        // H2 2.4 keeps a session comparator in CHECK IN constant sets; retain the DDL session.
        if (schemaConnection != null) {
            schemaConnection.close();
        }
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
        return catalogService.createSpu(
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
        productGateway.save(
                Product.create(
                "OLD", "old", null, null, BigDecimal.ONE, 3));
        productGateway.updateStatus(old.id(), "ON_SHELF");
        assertEquals(old.id(), productGateway.findSellableById(old.id()).id());
        assertTrue(productInventoryService.reserveStock("legacy", old.id(), 1));
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
        categoryGateway.saveCategory(new Category(null, categoryId, "child", null, 0, "ENABLED"))
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
        var spu = catalogService.createSpu(draft);
        Long sku = spu.skus().get(0).id();
        spuGateway.shelf(spu.id(), true);
        categoryGateway.saveCategory(new Category(categoryId, null, "root", null, 0, "DISABLED"));
        assertNull(productGateway.findSellableById(sku));
        assertFalse(productInventoryService.reserveStock("PARENT", sku, 1));
        categoryGateway.saveCategory(new Category(categoryId, null, "root", null, 0, "ENABLED"));
        brandGateway.saveBrand(new Brand(brandId, "brand", null, null, 0, "DISABLED"));
        assertNull(productGateway.findSellableById(sku));
        assertFalse(productInventoryService.reserveStock("BRAND", sku, 1));
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
        assertNull(productGateway.findSellableById(sku));
        assertFalse(productInventoryService.reserveStock("O0", sku, 1));
        spuGateway.shelf(spu.id(), true);
        assertTrue(productInventoryService.reserveStock("O1", sku, 2));
        assertTrue(productInventoryService.reserveStock("O2", sku, 1));
        categoryGateway.saveCategory(new Category(categoryId, null, "category", null, 0, "DISABLED"));
        assertNull(productGateway.findSellableById(sku));
        assertFalse(productInventoryService.reserveStock("O3", sku, 1));
        productInventoryService.confirmStock("O1", sku);
        productInventoryService.confirmStock("O1", sku);
        productInventoryService.releaseStock("O2", sku);
        productInventoryService.releaseStock("O2", sku);
        assertEquals(3, productGateway.findById(sku).stock());
    }

    @Test
    void ledgerIsIdempotentRejectsChangedPayloadAndRollsBackFailedAdjustment() throws Exception {
        var spu = create("SP1", draft("A", 5));
        Long sku = spu.skus().get(0).id();
        var request = new StockAdjustment("R1", sku, 3, "receipt", null);
        assertEquals(8, catalogService.adjustStock(request).resultingStock());
        assertEquals(8, catalogService.adjustStock(request).resultingStock());
        assertThrows(
                IllegalStateException.class,
                () -> catalogService.adjustStock(new StockAdjustment("R1", sku, 4, "receipt", null)));
        assertThrows(
                IllegalStateException.class,
                () -> catalogService.adjustStock(new StockAdjustment("R2", sku, -9, "count", null)));
        assertEquals(1, count("mall_stock_adjustment"));
        assertEquals(8, productGateway.findById(sku).stock());
        catalogService.adjustStock(new StockAdjustment("R3", sku, Integer.MAX_VALUE - 8, "limit", null));
        assertThrows(
                IllegalStateException.class,
                () -> catalogService.adjustStock(new StockAdjustment("R4", sku, 1, "overflow", null)));
    }

    @Test
    void adjustmentLeavesCapacityForOutstandingReservationRelease() {
        var spu = create("SP1", draft("A", 5));
        Long sku = spu.skus().get(0).id();
        spuGateway.shelf(spu.id(), true);
        assertTrue(productInventoryService.reserveStock("O1", sku, 2));
        assertThrows(
                IllegalStateException.class,
                () ->
                catalogService.adjustStock(
                new StockAdjustment(
                "MAX", sku, Integer.MAX_VALUE - 3, "receipt", null)));
        productInventoryService.releaseStock("O1", sku);
        assertEquals(5, productGateway.findById(sku).stock());
    }

    @Test
    void foreignKeysProtectReferencedTaxonomyIncludingOffShelfProducts() {
        create("SP1", draft("A", 5));
        assertThrows(IllegalStateException.class, () -> categoryGateway.deleteCategory(categoryId));
        assertThrows(IllegalStateException.class, () -> brandGateway.deleteBrand(brandId));
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
                        catalogService.adjustStock(
                                new StockAdjustment("C" + index, sku, -1, "count", null));
                        return true;
                    } catch (IllegalStateException exhausted) {
                        return false;
                    }
                });
            }
            long successes = 0;
            for (var future : executor.invokeAll(tasks)) {
                if (future.get()) {
                    successes++;
                }
            }
            assertEquals(5, successes);
            assertEquals(0, productGateway.findById(sku).stock());
            assertEquals(5, count("mall_stock_adjustment"));
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void cancellationTombstoneFencesDelayedReserveAndValidatesQuantity() {
        var spu = create("SP1", draft("A", 5));
        Long sku = spu.skus().get(0).id();
        spuGateway.shelf(spu.id(), true);
        assertEquals(1, productGateway.findSellableByIds(List.of(sku)).size());
        productInventoryService.cancelReservation(" CANCEL ", sku, 2);
        assertFalse(productInventoryService.reserveStock("CANCEL", sku, 2));
        productInventoryService.cancelReservation("CANCEL", sku, 2);
        assertThrows(IllegalStateException.class, () -> productInventoryService.reserveStock("CANCEL", sku, 3));
        assertThrows(IllegalStateException.class, () -> productInventoryService.confirmStock("CANCEL", sku));
        assertEquals(5, productGateway.findById(sku).stock());
    }

    @Test
    void concurrentDuplicateReservesAndCancelNeverDoubleDebitOrLeakInventory() throws Exception {
        var spu = create("SP1", draft("A", 5));
        Long sku = spu.skus().get(0).id();
        spuGateway.shelf(spu.id(), true);
        var pool = Executors.newFixedThreadPool(6);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                tasks.add(() -> productInventoryService.reserveStock("DUP", sku, 2));
            }
            for (var result : pool.invokeAll(tasks)) {
                assertTrue(result.get());
            }
            assertEquals(3, productGateway.findById(sku).stock());
            var cancel = pool.submit(() -> productInventoryService.cancelReservation("DUP", sku, 2));
            var retry = pool.submit(() -> productInventoryService.reserveStock("DUP", sku, 2));
            cancel.get();
            retry.get();
            assertFalse(productInventoryService.reserveStock("DUP", sku, 2));
            assertEquals(5, productGateway.findById(sku).stock());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void pagingAndMetadataChangesNeverReplaceSkuInventory() {
        var spu = create("SP1", draft("A", 5));
        assertEquals(1, spuGateway.page(new CatalogFilter("goods", null, 0, 10)).total());
        assertTrue(
                spuGateway.updateSpu(
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
        assertEquals(5, catalogService.spu(spu.id()).skus().get(0).stock());
        assertTrue(spuGateway.page(new CatalogFilter("missing", null, 0, 10)).records().isEmpty());
    }

    @Test
    void publicCatalogHidesOffShelfAndDisabledTaxonomyAndBrand() {
        var spu = create("PUBLIC", draft("a", 3));
        var f = new StoreFilter(null, null, 0, 20);
        assertEquals(0, spuGateway.storeCount(f));
        assertNull(catalogService.product(spu.id()));
        spuGateway.shelf(spu.id(), true);
        assertEquals(1, spuGateway.storeCount(f));
        assertTrue(spuGateway.products(f).get(0).available());
        assertEquals(1, catalogService.product(spu.id()).skus().size());
        categoryGateway.saveCategory(new Category(categoryId, null, "category", null, 0, "DISABLED"));
        assertEquals(0, spuGateway.storeCount(f));
        assertNull(catalogService.product(spu.id()));
        assertTrue(categoryGateway.visibleCategories().isEmpty());
        categoryGateway.saveCategory(new Category(categoryId, null, "category", null, 0, "ENABLED"));
        brandGateway.saveBrand(new Brand(brandId, "brand", null, null, 0, "DISABLED"));
        assertEquals(0, spuGateway.storeCount(f));
        assertNull(catalogService.product(spu.id()));
    }

    @Test
    void publicCatalogFiltersAncestorAndSearchWithBoundedPriceRange() throws Exception {
        Long parent = categoryId;
        categoryId =
        categoryGateway.saveCategory(new Category(null, parent, "child", null, 1, "ENABLED")).id();
        var first = draft("a", 0);
        var second =
        new CatalogSku(
                null,
                null,
                "b",
                "other",
                new BigDecimal("12.50"),
                3,
                null,
                Map.of("颜色", "b"),
                "ON_SHELF");
        var spu = create("RANGE", first, second);
        spuGateway.shelf(spu.id(), true);
        var f = new StoreFilter("goods", parent, 0, 1);
        assertEquals(1, spuGateway.storeCount(f));
        assertEquals(new BigDecimal("10.00"), spuGateway.products(f).get(0).minPrice());
        assertEquals(new BigDecimal("12.50"), spuGateway.products(f).get(0).maxPrice());
        assertEquals(
                0,
                spuGateway.storeCount(
                new StoreFilter("missing", null, 0, 20)));
        categoryGateway.saveCategory(new Category(parent, null, "parent", null, 0, "DISABLED"));
        assertTrue(categoryGateway.visibleCategories().isEmpty());
        assertNull(catalogService.product(spu.id()));
        assertEquals(0, spuGateway.storeCount(f));
    }

    @Test
    void publicCatalogKeepsOutOfStockProductsButRemovesHiddenSkus() throws Exception {
        var spu = create("SOLD", draft("a", 0), draft("b", 0));
        spuGateway.shelf(spu.id(), true);
        assertFalse(
                spuGateway.products(new StoreFilter(null, null, 0, 20))
                .get(0)
                .available());
        try (var st = schemaConnection.createStatement()) {
            st.execute(
                    "UPDATE mall_product SET status='OFF_SHELF' WHERE id="
                    + spu.skus().get(0).id());
        }
        assertEquals(1, catalogService.product(spu.id()).skus().size());
        try (var st = schemaConnection.createStatement()) {
            st.execute("UPDATE mall_product SET status='OFF_SHELF'");
        }
        assertNull(catalogService.product(spu.id()));
        assertEquals(
                0,
                spuGateway.storeCount(new StoreFilter(null, null, 0, 20)));
    }

    @Test
    void promotionFactsUseRealSpuAndExactPriceAndKeepZeroStock() throws Exception {
        var spu = create("PROMO", draft("P1", 0), draft("P2", 8));
        spuGateway.shelf(spu.id(), true);
        var ids = spu.skus().stream().map(CatalogSku::id).toList();
        var facts = productGateway.facts(ids);
        assertEquals(2, facts.size());
        assertEquals(spu.id().longValue(), facts.get(0).spuId());
        assertEquals(1000, facts.get(0).unitPriceFen());
        assertEquals(0, facts.get(0).availableStock());
        var hash = facts.get(1).hash();
        try (var st = schemaConnection.createStatement()) {
            st.execute("UPDATE mall_product SET stock=7 WHERE id=" + ids.get(1));
        }
        assertEquals(hash, productGateway.facts(ids).get(1).hash());
        try (var st = schemaConnection.createStatement()) {
            st.execute("UPDATE mall_product SET price=10.01 WHERE id=" + ids.get(1));
        }
        assertNotEquals(hash, productGateway.facts(ids).get(1).hash());
        assertEquals(1001, productGateway.facts(ids).get(1).unitPriceFen());
    }

    @Test
    void promotionFactsHideDisabledTaxonomyAndOffShelfSku() throws Exception {
        var spu = create("PROMO", draft("P1", 2), draft("P2", 8));
        spuGateway.shelf(spu.id(), true);
        var ids = spu.skus().stream().map(CatalogSku::id).toList();
        try (var st = schemaConnection.createStatement()) {
            st.execute("UPDATE mall_product SET status='OFF_SHELF' WHERE id=" + ids.get(0));
        }
        assertEquals(1, productGateway.facts(ids).size());
        try (var st = schemaConnection.createStatement()) {
            st.execute("UPDATE mall_brand SET status='DISABLED' WHERE id=" + brandId);
        }
        assertTrue(productGateway.facts(ids).isEmpty());
        try (var st = schemaConnection.createStatement()) {
            st.execute("UPDATE mall_brand SET status='ENABLED' WHERE id=" + brandId);
            st.execute("UPDATE mall_category SET status='DISABLED' WHERE id=" + categoryId);
        }
        assertTrue(productGateway.facts(ids).isEmpty());
        assertEquals(ids, productGateway.existingSkuIds(ids));
        assertEquals(List.of(spu.id()), spuGateway.existingSpuIds(List.of(spu.id())));
    }

    @Test
    void promotionFactsRejectOverflowAndLegacyUnmappedSku() throws Exception {
        var legacy =
        productGateway.save(
                Product.create(
                "OLD", "old", null, null, BigDecimal.ONE, 2));
        productGateway.updateStatus(legacy.id(), "ON_SHELF");
        assertTrue(productGateway.facts(List.of(legacy.id())).isEmpty());
        var spu = create("PROMO", draft("P1", 2));
        spuGateway.shelf(spu.id(), true);
        long sku = spu.skus().get(0).id();
        try (var st = schemaConnection.createStatement()) {
            st.execute("UPDATE mall_product SET price=99999999999999999.99 WHERE id=" + sku);
        }
        assertThrows(ArithmeticException.class, () -> productGateway.facts(List.of(sku)));
        assertThrows(
                IllegalArgumentException.class,
                () -> productGateway.facts(Collections.nCopies(101, sku)));
    }
}
