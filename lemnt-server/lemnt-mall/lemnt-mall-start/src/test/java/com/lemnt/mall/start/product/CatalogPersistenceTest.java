package com.lemnt.mall.start.product;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.mall.application.product.cmd.ProductSpecCreateCmd;
import com.lemnt.mall.application.product.cmd.ProductSpecUpdateCmd;
import com.lemnt.mall.application.product.cmd.ProductSpecValueCreateCmd;
import com.lemnt.mall.application.product.cmd.ProductSpecValueUpdateCmd;
import com.lemnt.mall.application.product.executor.BrandPageQueryExe;
import com.lemnt.mall.application.product.executor.CategoryTreeQueryExe;
import com.lemnt.mall.application.product.qry.BrandPageQry;
import com.lemnt.mall.application.product.service.ProductCatalogService;
import com.lemnt.mall.application.product.service.ProductInventoryService;
import com.lemnt.mall.application.product.service.ProductManagementService;
import com.lemnt.mall.application.product.service.ProductSpecService;
import com.lemnt.mall.domain.product.gateway.BrandGateway;
import com.lemnt.mall.domain.product.gateway.CategoryGateway;
import com.lemnt.mall.domain.product.gateway.ProductSkuGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpecGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpecValueGateway;
import com.lemnt.mall.domain.product.gateway.ProductSpuGateway;
import com.lemnt.mall.domain.product.model.Brand;
import com.lemnt.mall.domain.product.model.Category;
import com.lemnt.mall.domain.product.model.Product;
import com.lemnt.mall.domain.product.model.ProductCatalogFilter;
import com.lemnt.mall.domain.product.model.ProductCatalogValidation;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.product.model.ProductSpecFilter;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.domain.product.model.StoreFilter;
import com.lemnt.mall.start.TableTestComponents;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
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
    ProductSpuGateway productSpuGateway;
    ProductSkuGateway productSkuGateway;
    ProductCatalogService catalogService;
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
        ScriptUtils.executeSqlScript(schemaConnection, new ClassPathResource("sql/V16__product_management.sql"));
        var config = new MybatisConfiguration();
        var interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        config.addInterceptor(interceptor);
        config.setMapUnderscoreToCamelCase(true);
        var bean = new MybatisSqlSessionFactoryBean();
        bean.setMapperLocations(new org.springframework.core.io.support.PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/*.xml"));
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
        components.register(com.lemnt.mall.domain.member.gateway.MemberTradeGateway.class, new com.lemnt.mall.domain.member.gateway.MemberTradeGateway() {
            public com.lemnt.mall.domain.member.model.MemberPointConfig config() {
                return new com.lemnt.mall.domain.member.model.MemberPointConfig(false, 1, 0, 0);
            }
            public void apply(com.lemnt.mall.domain.member.model.MemberTradeChange change) { }
        });
        categoryGateway = components.get(CategoryGateway.class);
        brandGateway = components.get(BrandGateway.class);
        productSpuGateway = components.get(ProductSpuGateway.class);
        productSkuGateway = components.get(ProductSkuGateway.class);
        catalogService = components.get(ProductCatalogService.class);
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

    ProductSku draft(String code, int stock) {
        return new ProductSku(
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

    ProductSpu create(String code, ProductSku... skus) {
        return catalogService.createSpu(
                ProductCatalogValidation.newSpu(
                new ProductSpu(
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
        productSkuGateway.save(
                Product.create(
                "OLD", "old", null, null, BigDecimal.ONE, 3));
        productSkuGateway.updateStatus(old.id(), "ON_SHELF");
        assertEquals(old.id(), productSkuGateway.findSellableById(old.id()).id());
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
                    "INSERT INTO mall_product_sku(id,product_code,name,price,stock,status) "
                    + "VALUES(42,'LEGACY','legacy',9.25,7,'ON_SHELF')");
            ScriptUtils.executeSqlScript(
                    connection, new ClassPathResource("sql/V2__mall_catalog.sql"));
            try (var row =
                    statement.executeQuery(
                    "SELECT id,price,stock,spu_id FROM mall_product_sku WHERE id=42")) {
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
        ProductCatalogValidation.newSpu(
                new ProductSpu(
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
        productSpuGateway.shelf(spu.id(), true);
        categoryGateway.saveCategory(new Category(categoryId, null, "root", null, 0, "DISABLED"));
        assertNull(productSkuGateway.findSellableById(sku));
        assertFalse(productInventoryService.reserveStock("PARENT", sku, 1));
        categoryGateway.saveCategory(new Category(categoryId, null, "root", null, 0, "ENABLED"));
        brandGateway.saveBrand(new Brand(brandId, "brand", null, null, 0, "DISABLED"));
        assertNull(productSkuGateway.findSellableById(sku));
        assertFalse(productInventoryService.reserveStock("BRAND", sku, 1));
    }

    @Test
    void createsVariantsAndRollsBackWholeAggregateOnDuplicateSku() throws Exception {
        var first = create("SP1", draft("A", 4));
        assertEquals(Map.of("颜色", "A"), first.skus().get(0).properties());
        assertThrows(RuntimeException.class, () -> create("SP2", draft("B", 2), draft("A", 2)));
        assertEquals(1, count("mall_product_spu"));
        assertEquals(1, count("mall_product_sku"));
        assertEquals(1, count("mall_product_sku_spec"));
    }

    @Test
    void disabledTaxonomyAndOffShelfBlockNewReservationsButKeepExistingReservationRecoverable() {
        var spu = create("SP1", draft("A", 5));
        Long sku = spu.skus().get(0).id();
        assertNull(productSkuGateway.findSellableById(sku));
        assertFalse(productInventoryService.reserveStock("O0", sku, 1));
        productSpuGateway.shelf(spu.id(), true);
        assertTrue(productInventoryService.reserveStock("O1", sku, 2));
        assertTrue(productInventoryService.reserveStock("O2", sku, 1));
        categoryGateway.saveCategory(new Category(categoryId, null, "category", null, 0, "DISABLED"));
        assertNull(productSkuGateway.findSellableById(sku));
        assertFalse(productInventoryService.reserveStock("O3", sku, 1));
        productInventoryService.confirmStock("O1", sku);
        productInventoryService.confirmStock("O1", sku);
        productInventoryService.releaseStock("O2", sku);
        productInventoryService.releaseStock("O2", sku);
        assertEquals(3, productSkuGateway.findById(sku).stock());
    }



    @Test
    void foreignKeysProtectReferencedTaxonomyIncludingOffShelfProducts() {
        create("SP1", draft("A", 5));
        assertThrows(IllegalStateException.class, () -> categoryGateway.deleteCategory(categoryId));
        assertThrows(IllegalStateException.class, () -> brandGateway.deleteBrand(brandId));
    }


    @Test
    void cancellationTombstoneFencesDelayedReserveAndValidatesQuantity() {
        var spu = create("SP1", draft("A", 5));
        Long sku = spu.skus().get(0).id();
        productSpuGateway.shelf(spu.id(), true);
        assertEquals(1, productSkuGateway.findSellableByIds(List.of(sku)).size());
        productInventoryService.cancelReservation(" CANCEL ", sku, 2);
        assertFalse(productInventoryService.reserveStock("CANCEL", sku, 2));
        productInventoryService.cancelReservation("CANCEL", sku, 2);
        assertThrows(IllegalStateException.class, () -> productInventoryService.reserveStock("CANCEL", sku, 3));
        assertThrows(IllegalStateException.class, () -> productInventoryService.confirmStock("CANCEL", sku));
        assertEquals(5, productSkuGateway.findById(sku).stock());
    }

    @Test
    void concurrentDuplicateReservesAndCancelNeverDoubleDebitOrLeakInventory() throws Exception {
        var spu = create("SP1", draft("A", 5));
        Long sku = spu.skus().get(0).id();
        productSpuGateway.shelf(spu.id(), true);
        var pool = Executors.newFixedThreadPool(6);
        try {
            List<Callable<Boolean>> tasks = new ArrayList<>();
            for (int i = 0; i < 6; i++) {
                tasks.add(() -> productInventoryService.reserveStock("DUP", sku, 2));
            }
            for (var result : pool.invokeAll(tasks)) {
                assertTrue(result.get());
            }
            assertEquals(3, productSkuGateway.findById(sku).stock());
            var cancel = pool.submit(() -> productInventoryService.cancelReservation("DUP", sku, 2));
            var retry = pool.submit(() -> productInventoryService.reserveStock("DUP", sku, 2));
            cancel.get();
            retry.get();
            assertFalse(productInventoryService.reserveStock("DUP", sku, 2));
            assertEquals(5, productSkuGateway.findById(sku).stock());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void pagingAndMetadataChangesNeverReplaceSkuInventory() {
        var spu = create("SP1", draft("A", 5));
        assertEquals(1, productSpuGateway.page(new ProductCatalogFilter("goods", null, 0, 10, null, null)).total());
        assertTrue(
                productSpuGateway.updateSpu(
                new ProductSpu(
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
        assertTrue(productSpuGateway.page(new ProductCatalogFilter("missing", null, 0, 10, null, null)).records().isEmpty());
    }

    @Test
    void publicCatalogHidesOffShelfAndDisabledTaxonomyAndBrand() {
        var spu = create("PUBLIC", draft("a", 3));
        var f = new StoreFilter(null, null, 0, 20);
        assertEquals(0, productSpuGateway.storeCount(f));
        assertNull(catalogService.product(spu.id()));
        productSpuGateway.shelf(spu.id(), true);
        assertEquals(1, productSpuGateway.storeCount(f));
        assertTrue(productSpuGateway.products(f).get(0).available());
        assertEquals(1, catalogService.product(spu.id()).skus().size());
        categoryGateway.saveCategory(new Category(categoryId, null, "category", null, 0, "DISABLED"));
        assertEquals(0, productSpuGateway.storeCount(f));
        assertNull(catalogService.product(spu.id()));
        assertTrue(categoryGateway.visibleCategories().isEmpty());
        categoryGateway.saveCategory(new Category(categoryId, null, "category", null, 0, "ENABLED"));
        brandGateway.saveBrand(new Brand(brandId, "brand", null, null, 0, "DISABLED"));
        assertEquals(0, productSpuGateway.storeCount(f));
        assertNull(catalogService.product(spu.id()));
    }

    @Test
    void publicCatalogFiltersAncestorAndSearchWithBoundedPriceRange() throws Exception {
        Long parent = categoryId;
        categoryId =
        categoryGateway.saveCategory(new Category(null, parent, "child", null, 1, "ENABLED")).id();
        var first = draft("a", 0);
        var second =
        new ProductSku(
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
        productSpuGateway.shelf(spu.id(), true);
        var f = new StoreFilter("goods", parent, 0, 1);
        assertEquals(1, productSpuGateway.storeCount(f));
        assertEquals(new BigDecimal("10.00"), productSpuGateway.products(f).get(0).minPrice());
        assertEquals(new BigDecimal("12.50"), productSpuGateway.products(f).get(0).maxPrice());
        assertEquals(
                0,
                productSpuGateway.storeCount(
                new StoreFilter("missing", null, 0, 20)));
        categoryGateway.saveCategory(new Category(parent, null, "parent", null, 0, "DISABLED"));
        assertTrue(categoryGateway.visibleCategories().isEmpty());
        assertNull(catalogService.product(spu.id()));
        assertEquals(0, productSpuGateway.storeCount(f));
    }

    @Test
    void publicCatalogKeepsOutOfStockProductsButRemovesHiddenSkus() throws Exception {
        var spu = create("SOLD", draft("a", 0), draft("b", 0));
        productSpuGateway.shelf(spu.id(), true);
        assertFalse(
                productSpuGateway.products(new StoreFilter(null, null, 0, 20))
                .get(0)
                .available());
        try (var st = schemaConnection.createStatement()) {
            st.execute(
                    "UPDATE mall_product_sku SET status='OFF_SHELF' WHERE id="
                    + spu.skus().get(0).id());
        }
        assertEquals(1, catalogService.product(spu.id()).skus().size());
        try (var st = schemaConnection.createStatement()) {
            st.execute("UPDATE mall_product_sku SET status='OFF_SHELF'");
        }
        assertNull(catalogService.product(spu.id()));
        assertEquals(
                0,
                productSpuGateway.storeCount(new StoreFilter(null, null, 0, 20)));
    }



    @Test
    void recycledSpuMustBeRestoredBeforeShelfAndDeletedRowsCannotBeResurrected() throws Exception {
        var spu = create("RECYCLE", draft("RECYCLE-RED", 5));
        var service = components.get(ProductManagementService.class);
        productSpuGateway.shelf(spu.id(), true);
        assertThrows(IllegalStateException.class, () -> service.recycle(spu.id()));
        productSpuGateway.shelf(spu.id(), false);
        assertTrue(service.recycle(spu.id()));
        assertEquals(1, productSpuGateway.counts("goods", categoryId, brandId).recycled());
        assertFalse(productSpuGateway.shelf(spu.id(), true));
        assertTrue(service.restore(spu.id()));
        assertEquals("OFF_SHELF", productSpuGateway.findSpu(spu.id()).status());
        service.recycle(spu.id());
        assertTrue(service.delete(spu.id()));
        assertTrue(service.delete(spu.id()));
        assertNull(catalogService.spu(spu.id()));
        assertEquals("DELETED", productSkuGateway.catalogSku(spu.skus().get(0).id()).status());
        assertFalse(productSkuGateway.updateStatus(spu.skus().get(0).id(), "ON_SHELF"));
        assertEquals(0, productSpuGateway.page(new ProductCatalogFilter(null, null, 0, 20, null, null)).total());
        assertThrows(IllegalStateException.class, () -> service.restore(spu.id()));
        assertEquals(1, count("mall_product_spu"));
        assertEquals(1, count("mall_product_sku"));
    }


    @Test
    void activeReservationsBlockDeletionAndFailureRollsBackEarlierSkuChanges() throws Exception {
        var spu = create("ACTIVE", draft("A", 5), draft("B", 5));
        var service = components.get(ProductManagementService.class);
        var reserved = spu.skus().get(1);
        productSpuGateway.shelf(spu.id(), true);
        assertTrue(productInventoryService.reserveStock("HOLD", reserved.id(), 1));
        productSpuGateway.shelf(spu.id(), false);
        service.recycle(spu.id());
        assertThrows(IllegalStateException.class, () -> service.delete(spu.id()));
        assertEquals("RECYCLED", productSpuGateway.findSpu(spu.id()).status());
        assertEquals(2, productSkuGateway.catalogSkus(spu.id()).size());
        assertEquals("ON_SHELF", productSkuGateway.catalogSku(spu.skus().get(0).id()).status());
        productInventoryService.cancelReservation("HOLD", reserved.id(), 1);
        assertTrue(service.delete(spu.id()));
        assertFalse(productInventoryService.reserveStock("LATE", reserved.id(), 1));
    }

    @Test
    void batchDetailsPreserveRequestOrderAndSpecSnapshotsWhileFiltersAndCountsAgree() throws Exception {
        var first = create("BATCH1", draft("R1", 2));
        var second = create("BATCH2", draft("B2", 3));
        var service = components.get(ProductManagementService.class);
        var values = service.batchDetails(List.of(second.id(), first.id(), second.id()));
        assertEquals(List.of(second.id(), first.id()), values.stream().map(ProductSpu::id).toList());
        assertEquals(Map.of("颜色", "B2"), values.get(0).skus().get(0).properties());
        assertEquals(2, productSpuGateway.page(new ProductCatalogFilter("goods", null, 0, 20, categoryId, brandId)).total());
        assertEquals(0, productSpuGateway.page(new ProductCatalogFilter("goods", null, 0, 20, categoryId + 99, brandId)).total());
        assertEquals(2, productSpuGateway.counts("goods", categoryId, brandId).offShelf());
        assertThrows(IllegalArgumentException.class, () -> service.batchDetails(Collections.nCopies(101, first.id())));
    }

    @Test
    void propertyDictionaryProtectsReferencedNamesAndClearsOptionalRemarks() throws Exception {
        create("DICT", draft("RED", 2));
        var service = components.get(ProductSpecService.class);
        var property = service.create(new ProductSpecCreateCmd("颜色", "说明"));
        var value = service.createValue(new ProductSpecValueCreateCmd(property.id(), "RED", "值说明"));
        assertThrows(IllegalStateException.class,
                () -> service.update(new ProductSpecUpdateCmd(property.id(), "色彩", null)));
        assertThrows(IllegalStateException.class,
                () -> service.updateValue(new ProductSpecValueUpdateCmd(value.id(), "红色", null)));
        assertThrows(IllegalStateException.class, () -> service.deleteValue(value.id()));
        assertThrows(IllegalStateException.class, () -> service.delete(property.id()));
        assertNull(service.update(new ProductSpecUpdateCmd(property.id(), "颜色", null)).remark());
        assertNull(service.updateValue(new ProductSpecValueUpdateCmd(value.id(), "RED", null)).remark());
        var unused = service.createValue(new ProductSpecValueCreateCmd(property.id(), "GREEN", null));
        assertTrue(service.deleteValue(unused.id()));
        var page = components.get(ProductSpecValueGateway.class).page(new ProductSpecFilter("RED", property.id(), 1, 20));
        assertEquals(1, page.getTotal());
        assertEquals(value.id(), page.getRecords().get(0).id());
    }

    @Test
    void propertyMigrationCopiesLegacySnapshotsWithoutRewritingSkuCombinationKeys() throws Exception {
        create("BACKFILL", draft("RED", 2), draft("BLUE", 2));
        try (var statement = schemaConnection.createStatement()) {
            statement.execute("DROP TABLE mall_product_comment");
            statement.execute("DROP TABLE mall_product_spec_value");
            statement.execute("DROP TABLE mall_product_spec");
        }
        ScriptUtils.executeSqlScript(schemaConnection, new ClassPathResource("sql/V16__product_management.sql"));
        assertEquals(1, count("mall_product_spec"));
        assertEquals(2, count("mall_product_spec_value"));
        assertEquals(2, count("mall_product_sku_spec"));
        assertEquals(1, components.get(ProductSpecGateway.class).page(new ProductSpecFilter("颜色", null, 1, 20)).getTotal());
    }

    @Test
    void brandPaginationAndCategoryTreeExposeActualRowsWithoutListTruncation() throws Exception {
        var child = categoryGateway.saveCategory(new Category(null, categoryId, "子类", null, 1, "ENABLED"));
        var tree = components.get(CategoryTreeQueryExe.class).execute();
        assertEquals(child.id(), tree.get(0).children().get(0).id());
        try (var connection = source.getConnection(); var insert = connection.prepareStatement("INSERT INTO mall_product_brand(name) VALUES(?)")) {
            for (int i = 0; i < 1001; i++) { insert.setString(1, "品牌-" + i); insert.addBatch(); }
            insert.executeBatch();
        }
        var page = components.get(BrandPageQueryExe.class).execute(new BrandPageQry("品牌-", "ENABLED", 2, 20));
        assertEquals(1001, page.getTotal());
        assertEquals(20, page.getRecords().size());
        assertThrows(IllegalStateException.class, () -> brandGateway.brands());
    }


    @Test
    void productCommandsPersistDeliveryTemplateAndSkuDimensionsWithoutLosingThemOnUpdate() {
        var create = components.get(com.lemnt.mall.application.product.executor.ProductSpuCreateCmdExe.class);
        var saved = create.execute(new com.lemnt.mall.application.product.cmd.ProductSpuCreateCmd(
                "DELIVERY-FIELDS", "快递商品", categoryId, brandId, null, null,
                List.of(new com.lemnt.mall.application.product.cmd.ProductSpuCreateCmd.ProductSkuDraft(
                        "DELIVERY-SKU", "规格", BigDecimal.TEN, 5, null, Map.of(),
                        new BigDecimal("1.25"), new BigDecimal("0.125"))), List.of(1), 99L));
        assertEquals(List.of(1), saved.deliveryTypes());
        assertEquals(99L, saved.deliveryTemplateId());
        assertEquals(0, saved.skus().get(0).weight().compareTo(new BigDecimal("1.25")));
        var sku = catalogService.spu(saved.id()).skus().get(0);
        var replacement = new ProductSku(sku.id(), saved.id(), sku.code(), sku.name(), sku.price(), sku.stock(),
                sku.image(), sku.properties(), sku.status(), new BigDecimal("2.75"), new BigDecimal("0.25"));
        assertTrue(components.get(com.lemnt.mall.application.product.executor.ProductSpuUpdateCmdExe.class).execute(
                new com.lemnt.mall.application.product.cmd.ProductSpuUpdateCmd(saved.id(), saved.name(), categoryId,
                        brandId, null, null, List.of(replacement), List.of(1), 100L)));
        var updated = catalogService.spu(saved.id());
        assertEquals(100L, updated.deliveryTemplateId());
        assertEquals(0, updated.skus().get(0).weight().compareTo(new BigDecimal("2.75")));
        assertEquals(0, updated.skus().get(0).volume().compareTo(new BigDecimal("0.25")));
    }


    /** 编号与名称逐项配对，不能依赖Map遍历顺序。 */
    @Test
    void dictionaryIdentitiesPersistWithCanonicalNamesAndProtectReferences() {
        var service = components.get(ProductSpecService.class);
        var color = service.create(new ProductSpecCreateCmd("颜色字典", null));
        var size = service.create(new ProductSpecCreateCmd("尺寸字典", null));
        var red = service.createValue(new ProductSpecValueCreateCmd(color.id(), "红色", null));
        var large = service.createValue(new ProductSpecValueCreateCmd(size.id(), "大号", null));
        var ids = Map.of(color.id(), red.id(), size.id(), large.id());
        var saved = catalogService.createSpu(identitySpu("LINKED", identitySku("A", ids)));
        var sku = catalogService.sku(saved.skus().get(0).id());
        assertEquals(ids, sku.specValueIds());
        assertEquals(Map.of("颜色字典", "红色", "尺寸字典", "大号"), sku.properties());
        assertEquals(ids, catalogService.spu(saved.id()).skus().get(0).specValueIds());
        assertEquals(ids, components.get(ProductManagementService.class).batchDetails(List.of(saved.id()))
                .get(0).skus().get(0).specValueIds());
        assertThrows(IllegalStateException.class, () -> service.deleteValue(red.id()));
        assertThrows(IllegalStateException.class, () -> service.updateValue(
                new ProductSpecValueUpdateCmd(red.id(), "已改名", null)));
    }

    /** 错误归属、未知编号、SKU规格项不一致全部拒绝，并且不创建半个商品。 */
    @Test
    void invalidDictionarySelectionIsRejectedBeforeAnyProductWrite() throws Exception {
        var service = components.get(ProductSpecService.class);
        var a = service.create(new ProductSpecCreateCmd("规格甲", null));
        var b = service.create(new ProductSpecCreateCmd("规格乙", null));
        var option = service.createValue(new ProductSpecValueCreateCmd(a.id(), "值甲", null));
        long before = count("mall_product_spu");
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> catalogService.createSpu(
                identitySpu("WRONG", identitySku("A", Map.of(b.id(), option.id())))));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> catalogService.createSpu(
                identitySpu("MISSING", identitySku("A", Map.of(a.id(), Long.MAX_VALUE)))));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> catalogService.createSpu(
                identitySpu("MIXED", identitySku("A", Map.of(a.id(), option.id())), identitySku("B", Map.of()))));
        var forged = new ProductSku(null, null, "FORGED", "sku", BigDecimal.TEN, 1, null,
                Map.of("规格甲", "伪造"), "ON_SHELF", null, null, Map.of(a.id(), option.id()));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> catalogService.createSpu(identitySpu("FORGED", forged)));
        assertEquals(before, count("mall_product_spu"));
        assertEquals(0, count("mall_product_sku_spec"));
    }

    /** 历史同名数据不推断字典编号，只有显式选择后才建立关联。 */
    @Test
    void legacyPropertiesRemainUnlinkedUntilExplicitSelectionAndUpdateReplacesIdentity() {
        var legacy = create("LEGACY-IDENTITY", draft("旧值", 5));
        var id = legacy.skus().get(0).id();
        assertNull(catalogService.sku(id).specValueIds());
        var service = components.get(ProductSpecService.class);
        var spec = service.create(new ProductSpecCreateCmd("颜色", null));
        var value = service.createValue(new ProductSpecValueCreateCmd(spec.id(), "旧值", null));
        assertNull(catalogService.sku(id).specValueIds());
        var ids = Map.of(spec.id(), value.id());
        var linked = new ProductSku(id, legacy.id(), "旧值", "sku", BigDecimal.TEN, 5, null, Map.of(),
                "ON_SHELF", null, null, ids);
        assertTrue(catalogService.updateSpu(new ProductSpu(legacy.id(), legacy.code(), legacy.name(), categoryId,
                brandId, null, null, legacy.status(), List.of(linked))));
        assertEquals(ids, catalogService.sku(id).specValueIds());
        assertEquals(Map.of("颜色", "旧值"), catalogService.sku(id).properties());
    }

    private ProductSku identitySku(String code, Map<Long, Long> ids) {
        return new ProductSku(null, null, code, "sku", BigDecimal.TEN, 5, null, Map.of(),
                "ON_SHELF", null, null, ids);
    }

    private ProductSpu identitySpu(String code, ProductSku... skus) {
        return new ProductSpu(null, code, "关联商品", categoryId, brandId, null, null, null, List.of(skus));
    }

    /** 第二个SKU写入失败时，主体、首个SKU及字典关系一起回滚。 */
    @Test
    void dictionaryIdentityWritesRollBackWithWholeProductOnLaterSkuFailure() throws Exception {
        var service = components.get(ProductSpecService.class);
        var spec = service.create(new ProductSpecCreateCmd("回滚规格", null));
        var first = service.createValue(new ProductSpecValueCreateCmd(spec.id(), "甲", null));
        var second = service.createValue(new ProductSpecValueCreateCmd(spec.id(), "乙", null));
        try (var statement = schemaConnection.createStatement()) {
            statement.execute("ALTER TABLE mall_product_sku ADD CONSTRAINT reject_second_spec CHECK(product_code <> 'SECOND-FAIL')");
        }
        assertThrows(RuntimeException.class, () -> catalogService.createSpu(identitySpu("ROLLBACK-SPEC",
                identitySku("FIRST", Map.of(spec.id(), first.id())),
                identitySku("SECOND-FAIL", Map.of(spec.id(), second.id())))));
        assertEquals(0, count("mall_product_spu"));
        assertEquals(0, count("mall_product_sku"));
        assertEquals(0, count("mall_product_sku_spec"));
        assertNotNull(service.createValue(new ProductSpecValueCreateCmd(spec.id(), "丙", null)));
    }
}
