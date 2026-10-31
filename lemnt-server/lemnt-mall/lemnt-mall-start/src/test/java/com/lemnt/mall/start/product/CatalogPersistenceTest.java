package com.lemnt.mall.start.product;

import com.lemnt.common.mybatis.handler.MyMetaObjectHandler;
import com.lemnt.mall.application.product.cmd.ProductSpecCreateCmd;
import com.lemnt.mall.application.product.cmd.ProductSpuCreateCmd;
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
        assertEquals(List.of(new com.lemnt.mall.domain.product.model.ProductSkuSpec(null, "颜色", null, "A")), first.skus().get(0).properties());
        assertThrows(RuntimeException.class, () -> create("SP2", draft("B", 2), draft("A", 2)));
        assertEquals(1, count("mall_product_spu"));
        assertEquals(1, count("mall_product_sku"));
        assertEquals(1, storedPropertyCount());
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
        assertEquals(1, productSpuGateway.page(new ProductCatalogFilter("goods", null, 0, 10, null, null, null)).total());
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
        assertTrue(productSpuGateway.page(new ProductCatalogFilter("missing", null, 0, 10, null, null, null)).records().isEmpty());
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
        assertEquals(0, productSpuGateway.page(new ProductCatalogFilter(null, null, 0, 20, null, null, null)).total());
        assertThrows(IllegalStateException.class, () -> service.restore(spu.id()));
        assertEquals(1, count("mall_product_spu"));
        assertEquals(1, count("mall_product_sku"));
    }


    /** 库存分组取SKU总量而非任意一行；售罄包含回收站，警戒排除回收站。 */
    @Test
    void stockTabsAggregateAllActiveSkuStockAndFollowReservationsAndSourceBoundaries() throws Exception {
        var zero = create("ZERO", draft("Z", 0));
        var ten = create("TEN", draft("T", 10));
        var mixed = create("MIXED", draft("M0", 0), draft("M11", 11));
        var recycled = create("RZERO", draft("RZ", 0));
        var deleted = create("DZERO", draft("DZ", 0));
        var management = components.get(ProductManagementService.class);
        management.recycle(recycled.id());
        management.recycle(deleted.id());
        management.delete(deleted.id());
        var counts = productSpuGateway.counts("goods", categoryId, brandId);
        assertEquals(2, counts.soldOut());
        assertEquals(2, counts.lowStock());
        var sold = new ProductCatalogFilter(null, null, 0, 1, categoryId, brandId,
                com.lemnt.mall.domain.product.model.ProductStockStatus.SOLD_OUT);
        assertEquals(2, productSpuGateway.page(sold).total());
        assertEquals(List.of(recycled.id()), productSpuGateway.page(sold).records().stream().map(ProductSpu::id).toList());
        var low = new ProductCatalogFilter(null, null, 0, 20, categoryId, brandId,
                com.lemnt.mall.domain.product.model.ProductStockStatus.LOW_STOCK);
        assertEquals(List.of(ten.id(), zero.id()), productSpuGateway.page(low).records().stream().map(ProductSpu::id).toList());
        assertEquals(0, productSpuGateway.counts("missing", categoryId, brandId).soldOut());
        assertEquals(0, productSpuGateway.counts(null, categoryId, brandId + 100).lowStock());
        productSpuGateway.shelf(mixed.id(), true);
        var sku = mixed.skus().get(1);
        assertTrue(productInventoryService.reserveStock("WARNING", sku.id(), 1));
        assertEquals(3, productSpuGateway.counts(null, categoryId, brandId).lowStock());
        assertEquals(3, productSpuGateway.page(low).total());
        productInventoryService.cancelReservation("WARNING", sku.id(), 1);
        assertEquals(2, productSpuGateway.counts(null, categoryId, brandId).lowStock());
        var exporter = components.get(com.lemnt.mall.application.product.executor.ProductSpuExportQueryExe.class);
        var exported = exporter.execute(new com.lemnt.mall.application.product.qry.ProductSpuExportQry(
                "goods", null, categoryId, brandId, "LOW_STOCK"));
        assertEquals(List.of(ten.id(), zero.id()), exported.stream().map(row -> row.id()).toList());
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
        assertEquals(List.of(new com.lemnt.mall.domain.product.model.ProductSkuSpec(null, "颜色", null, "B2")), values.get(0).skus().get(0).properties());
        assertEquals(2, productSpuGateway.page(new ProductCatalogFilter("goods", null, 0, 20, categoryId, brandId, null)).total());
        assertEquals(0, productSpuGateway.page(new ProductCatalogFilter("goods", null, 0, 20, categoryId + 99, brandId, null)).total());
        assertEquals(2, productSpuGateway.counts("goods", categoryId, brandId).offShelf());
        assertThrows(IllegalArgumentException.class, () -> service.batchDetails(Collections.nCopies(101, first.id())));
    }

    @Test
    void propertyDictionaryRenamesCurrentRowsWithoutGuessingLegacyIds() throws Exception {
        var legacy = create("DICT", draft("RED", 2));
        var service = components.get(ProductSpecService.class);
        var property = service.create(new ProductSpecCreateCmd("颜色", "说明"));
        var value = service.createValue(new ProductSpecValueCreateCmd(property.id(), "RED", "值说明"));
        assertNull(service.update(new ProductSpecUpdateCmd(property.id(), "色彩", null)).remark());
        assertNull(service.updateValue(new ProductSpecValueUpdateCmd(value.id(), "红色", null)).remark());
        assertEquals("颜色", catalogService.sku(legacy.skus().get(0).id()).properties().get(0).propertyName());
        assertEquals("RED", catalogService.sku(legacy.skus().get(0).id()).properties().get(0).valueName());
        assertThrows(IllegalStateException.class, () -> service.delete(property.id()));
        assertTrue(service.deleteValue(value.id()));
        assertTrue(service.delete(property.id()));
    }

    /** 创建时间使用表中事实，分页筛选包含边界且保留规格值归属。 */
    @Test
    void specCreationTimeFlowsThroughPagesAndInclusiveFiltersWithoutChangingOnRename() throws Exception {
        var service = components.get(ProductSpecService.class);
        var color = service.create(new ProductSpecCreateCmd("颜色", null));
        var size = service.create(new ProductSpecCreateCmd("尺码", null));
        var red = service.createValue(new ProductSpecValueCreateCmd(color.id(), "红", null));
        var blue = service.createValue(new ProductSpecValueCreateCmd(color.id(), "蓝", null));
        var large = service.createValue(new ProductSpecValueCreateCmd(size.id(), "大", null));
        assertNotNull(color.createTime());
        assertNotNull(red.createTime());
        var boundary = java.time.LocalDateTime.of(2026, 10, 15, 12, 0);
        for (String table : List.of("mall_product_spec", "mall_product_spec_value")) {
            try (var statement = schemaConnection.prepareStatement("UPDATE " + table + " SET create_time=?")) {
                statement.setObject(1, boundary.plusDays(1));
                statement.executeUpdate();
            }
        }
        try (var statement = schemaConnection.prepareStatement("UPDATE mall_product_spec SET create_time=? WHERE id=?")) {
            statement.setObject(1, boundary);
            statement.setLong(2, color.id());
            statement.executeUpdate();
        }
        for (Long id : List.of(red.id(), large.id())) {
            try (var statement = schemaConnection.prepareStatement("UPDATE mall_product_spec_value SET create_time=? WHERE id=?")) {
                statement.setObject(1, boundary);
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        }
        var specs = components.get(com.lemnt.mall.application.product.executor.ProductSpecPageQueryExe.class);
        var values = components.get(com.lemnt.mall.application.product.executor.ProductSpecValuePageQueryExe.class);
        assertEquals(List.of(size.id(), color.id()), specs.execute(
                new com.lemnt.mall.application.product.qry.ProductSpecPageQry(null, 1, 20, null, null))
                .getRecords().stream().map(row -> row.id()).toList());
        assertEquals(List.of(blue.id(), red.id()), values.execute(
                new com.lemnt.mall.application.product.qry.ProductSpecValuePageQry(color.id(), null, 1, 20))
                .getRecords().stream().map(row -> row.id()).toList());
        var result = specs.execute(new com.lemnt.mall.application.product.qry.ProductSpecPageQry(
                null, 1, 20, boundary, boundary));
        assertEquals(List.of(color.id()), result.getRecords().stream().map(row -> row.id()).toList());
        assertEquals(boundary, result.getRecords().get(0).createTime());
        var valueResult = values.execute(new com.lemnt.mall.application.product.qry.ProductSpecValuePageQry(
                color.id(), "红", 1, 20));
        assertEquals(List.of(red.id()), valueResult.getRecords().stream().map(row -> row.id()).toList());
        assertEquals(boundary, valueResult.getRecords().get(0).createTime());
        assertEquals(boundary, service.update(new ProductSpecUpdateCmd(color.id(), "色彩", null)).createTime());
        assertEquals(boundary, service.updateValue(new ProductSpecValueUpdateCmd(red.id(), "红色", null)).createTime());
        assertThrows(IllegalArgumentException.class, () -> specs.execute(
                new com.lemnt.mall.application.product.qry.ProductSpecPageQry(null, 1, 20, boundary.plusDays(1), boundary)));
    }

    @Test
    void freshDictionaryInitializationDoesNotInventIdsForLegacySkuJson() throws Exception {
        create("BACKFILL", draft("RED", 2), draft("BLUE", 2));
        try (var statement = schemaConnection.createStatement()) {
            statement.execute("DROP TABLE mall_product_comment");
            statement.execute("DROP TABLE mall_product_spec_value");
            statement.execute("DROP TABLE mall_product_spec");
        }
        ScriptUtils.executeSqlScript(schemaConnection, new ClassPathResource("sql/V16__product_management.sql"));
        assertEquals(0, count("mall_product_spec"));
        assertEquals(0, count("mall_product_spec_value"));
        assertEquals(2, storedPropertyCount());
        assertEquals(0, components.get(ProductSpecGateway.class).page(new ProductSpecFilter("颜色", null, 1, 20, null, null)).getTotal());
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


    /** 经营字段贯通写入、修改、字典重组与两种分页，轮播顺序必须保留。 */
    @Test
    void spuOperatingFieldsPersistAndLegacyInternalUpdatesPreserveThem() {
        var create = components.get(com.lemnt.mall.application.product.executor.ProductSpuCreateCmdExe.class);
        var draft = new com.lemnt.mall.application.product.cmd.ProductSpuCreateCmd.ProductSkuDraft(
                "META-SKU", "规格", BigDecimal.TEN, 3, null, List.of());
        var saved = create.execute(new ProductSpuCreateCmd("META", "商品", categoryId, brandId, "详情", null,
                List.of(draft), List.of(), null, "关键字", "简介", List.of("/one.png", "/two.png"), 20));
        var newer = create("NEWER", draft("NEW-SKU", 3));
        assertEquals("关键字", saved.keyword());
        assertEquals("简介", saved.introduction());
        assertEquals(List.of("/one.png", "/two.png"), saved.sliderPicUrls());
        assertEquals(20, saved.sort());
        var rows = productSpuGateway.page(new ProductCatalogFilter(null, null, 0, 20, categoryId, brandId, null));
        assertEquals(List.of(saved.id(), newer.id()), rows.records().stream().map(ProductSpu::id).toList());
        var low = productSpuGateway.page(new ProductCatalogFilter(null, null, 0, 20, categoryId, brandId,
                com.lemnt.mall.domain.product.model.ProductStockStatus.LOW_STOCK));
        assertEquals(List.of(saved.id(), newer.id()), low.records().stream().map(ProductSpu::id).toList());
        var update = components.get(com.lemnt.mall.application.product.executor.ProductSpuUpdateCmdExe.class);
        assertTrue(update.execute(new com.lemnt.mall.application.product.cmd.ProductSpuUpdateCmd(
                saved.id(), "商品修改", categoryId, brandId, "详情", null, catalogService.spu(saved.id()).skus(),
                List.of(), null, "新关键字", "新简介", List.of("/two.png", "/one.png"), 10)));
        var changed = catalogService.spu(saved.id());
        assertEquals(List.of("/two.png", "/one.png"), changed.sliderPicUrls());
        assertEquals("新简介", changed.introduction());
        assertTrue(update.execute(new com.lemnt.mall.application.product.cmd.ProductSpuUpdateCmd(
                saved.id(), "内部旧调用", categoryId, brandId, "详情", null, changed.skus())));
        changed = catalogService.spu(saved.id());
        assertEquals("新关键字", changed.keyword());
        assertEquals(List.of("/two.png", "/one.png"), changed.sliderPicUrls());
        assertEquals(10, changed.sort());
    }

    @Test
    void productCommandsPersistDeliveryTemplateAndSkuDimensionsWithoutLosingThemOnUpdate() {
        var create = components.get(com.lemnt.mall.application.product.executor.ProductSpuCreateCmdExe.class);
        var saved = create.execute(new com.lemnt.mall.application.product.cmd.ProductSpuCreateCmd(
                "DELIVERY-FIELDS", "快递商品", categoryId, brandId, null, null,
                List.of(new com.lemnt.mall.application.product.cmd.ProductSpuCreateCmd.ProductSkuDraft(
                        "DELIVERY-SKU", "规格", BigDecimal.TEN, 5, null, List.of(),
                        new BigDecimal("1.25"), new BigDecimal("0.125"))), List.of(1), 99L, null, null, null, null));
        assertEquals(List.of(1), saved.deliveryTypes());
        assertEquals(99L, saved.deliveryTemplateId());
        assertEquals(0, saved.skus().get(0).weight().compareTo(new BigDecimal("1.25")));
        var sku = catalogService.spu(saved.id()).skus().get(0);
        var replacement = new ProductSku(sku.id(), saved.id(), sku.code(), sku.name(), sku.price(), sku.stock(),
                sku.image(), sku.properties(), sku.status(), new BigDecimal("2.75"), new BigDecimal("0.25"));
        assertTrue(components.get(com.lemnt.mall.application.product.executor.ProductSpuUpdateCmdExe.class).execute(
                new com.lemnt.mall.application.product.cmd.ProductSpuUpdateCmd(saved.id(), saved.name(), categoryId,
                        brandId, null, null, List.of(replacement), List.of(1), 100L, null, null, null, null)));
        var updated = catalogService.spu(saved.id());
        assertEquals(100L, updated.deliveryTemplateId());
        assertEquals(0, updated.skus().get(0).weight().compareTo(new BigDecimal("2.75")));
        assertEquals(0, updated.skus().get(0).volume().compareTo(new BigDecimal("0.25")));
    }


    /** 编号与名称逐项配对，不能依赖Map遍历顺序。 */
    @Test
    void dictionaryIdentitiesPersistAndRenameCurrentSkuWhileDeletedValuesKeepSnapshots() {
        var service = components.get(ProductSpecService.class);
        var color = service.create(new ProductSpecCreateCmd("颜色字典", null));
        var size = service.create(new ProductSpecCreateCmd("尺寸字典", null));
        var red = service.createValue(new ProductSpecValueCreateCmd(color.id(), "红色", null));
        var large = service.createValue(new ProductSpecValueCreateCmd(size.id(), "大号", null));
        var ids = Map.of(color.id(), red.id(), size.id(), large.id());
        var saved = catalogService.createSpu(identitySpu("LINKED", identitySku("A", ids)));
        var sku = catalogService.sku(saved.skus().get(0).id());
        assertEquals(ids, identity(sku.properties()));
        assertEquals(java.util.Set.of(new com.lemnt.mall.domain.product.model.ProductSkuSpec(color.id(), "颜色字典", red.id(), "红色"), new com.lemnt.mall.domain.product.model.ProductSkuSpec(size.id(), "尺寸字典", large.id(), "大号")), new java.util.HashSet<>(sku.properties()));
        assertEquals(ids, identity(catalogService.spu(saved.id()).skus().get(0).properties()));
        assertEquals(ids, identity(components.get(ProductManagementService.class).batchDetails(List.of(saved.id()))
                .get(0).skus().get(0).properties()));
        service.update(new ProductSpecUpdateCmd(color.id(), "色彩字典", null));
        service.updateValue(new ProductSpecValueUpdateCmd(red.id(), "已改名", null));
        var renamed = catalogService.sku(sku.id());
        assertTrue(renamed.properties().contains(new com.lemnt.mall.domain.product.model.ProductSkuSpec(color.id(), "色彩字典", red.id(), "已改名")));
        assertEquals(sku.price(), renamed.price());
        assertEquals(sku.stock(), renamed.stock());
        assertTrue(service.deleteValue(red.id()));
        assertTrue(service.delete(color.id()));
        assertEquals(renamed.properties(), catalogService.sku(sku.id()).properties());
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
                List.of(new com.lemnt.mall.domain.product.model.ProductSkuSpec(a.id(), "规格甲", option.id(), "伪造")), "ON_SHELF", null, null);
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> catalogService.createSpu(identitySpu("FORGED", forged)));
        assertEquals(before, count("mall_product_spu"));
        assertEquals(0, storedPropertyCount());
    }

    /** 历史同名数据不推断字典编号，只有显式选择后才建立关联。 */
    @Test
    void legacyPropertiesRemainUnlinkedUntilExplicitSelectionAndUpdateReplacesIdentity() {
        var legacy = create("LEGACY-IDENTITY", draft("旧值", 5));
        var id = legacy.skus().get(0).id();
        assertNull(catalogService.sku(id).properties().get(0).propertyId());
        var service = components.get(ProductSpecService.class);
        var spec = service.create(new ProductSpecCreateCmd("颜色", null));
        var value = service.createValue(new ProductSpecValueCreateCmd(spec.id(), "旧值", null));
        assertNull(catalogService.sku(id).properties().get(0).propertyId());
        var ids = Map.of(spec.id(), value.id());
        var linked = new ProductSku(id, legacy.id(), "旧值", "sku", BigDecimal.TEN, 5, null, ids.entrySet().stream().map(entry ->
                        new com.lemnt.mall.domain.product.model.ProductSkuSpec(entry.getKey(), null, entry.getValue(), null)).toList(),
                "ON_SHELF", null, null);
        assertTrue(catalogService.updateSpu(new ProductSpu(legacy.id(), legacy.code(), legacy.name(), categoryId,
                brandId, null, null, legacy.status(), List.of(linked))));
        assertEquals(ids, identity(catalogService.sku(id).properties()));
        assertEquals(List.of(new com.lemnt.mall.domain.product.model.ProductSkuSpec(spec.id(), "颜色", value.id(), "旧值")), catalogService.sku(id).properties());
    }

    private ProductSku identitySku(String code, Map<Long, Long> ids) {
        return new ProductSku(null, null, code, "sku", BigDecimal.TEN, 5, null, ids.entrySet().stream().map(entry ->
                        new com.lemnt.mall.domain.product.model.ProductSkuSpec(entry.getKey(), null, entry.getValue(), null)).toList(),
                "ON_SHELF", null, null);
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
        assertEquals(0, storedPropertyCount());
        assertNotNull(service.createValue(new ProductSpecValueCreateCmd(spec.id(), "丙", null)));
    }

    private Map<Long, Long> identity(List<com.lemnt.mall.domain.product.model.ProductSkuSpec> values) {
        return values.stream().collect(java.util.stream.Collectors.toMap(
                com.lemnt.mall.domain.product.model.ProductSkuSpec::propertyId,
                com.lemnt.mall.domain.product.model.ProductSkuSpec::valueId));
    }

    /** 真实Controller、校验、字典锁和事务落库；不代表网关认证或部署HTTP验收。 */
    @Test
    void propertyListHttpContractPersistsRealDictionaryIdsAndRejectsInvalidSelections() throws Exception {
        var dict = components.get(ProductSpecService.class);
        var spec = dict.create(new ProductSpecCreateCmd("接口颜色", null));
        var option = dict.createValue(new ProductSpecValueCreateCmd(spec.id(), "红色", null));
        var controller = new com.lemnt.mall.adapter.web.product.ProductSpuController(
                org.mockito.Mockito.mock(com.lemnt.mall.application.product.executor.ProductSpuPageQueryExe.class),
                org.mockito.Mockito.mock(com.lemnt.mall.application.product.executor.ProductSpuDetailQueryExe.class),
                components.get(com.lemnt.mall.application.product.executor.ProductSpuCreateCmdExe.class),
                org.mockito.Mockito.mock(com.lemnt.mall.application.product.executor.ProductSpuUpdateCmdExe.class),
                org.mockito.Mockito.mock(com.lemnt.mall.application.product.executor.ProductSpuShelfCmdExe.class));
        var builder = tools.jackson.databind.json.JsonMapper.builder();
        new com.lemnt.common.start.config.JacksonConfig().customizer().customize(builder);
        var mapper = builder.build();
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new com.lemnt.common.security.handler.GlobalExceptionHandler())
                .setMessageConverters(new org.springframework.http.converter.json.JacksonJsonHttpMessageConverter(mapper))
                .build();
        String payload = """
                {"code":"LIST-HTTP","name":"商品","categoryId":"%s","brandId":"%s","deliveryTypes":[],"keyword":"商品关键字","introduction":"简介","sliderPicUrls":[],"sort":0,
                 "skus":[{"code":"SKU-LIST-HTTP","name":"红色款","price":"10.00","stock":2,
                 "properties":[{"propertyId":"%s","valueId":"%s"}]}]}
                """.formatted(categoryId, brandId, spec.id(), option.id());
        var result = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/mall/spus")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content(payload))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.skus[0].properties[0].propertyName").value("接口颜色"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.keyword").value("商品关键字"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.sort").value(0))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.skus[0].properties[0].valueId").value(option.id().toString()))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.skus[0].specValueIds").doesNotExist())
                .andReturn();
        var savedId = mapper.readTree(result.getResponse().getContentAsString()).get("data").get("skus").get(0).get("id").asString();
        assertEquals(List.of(new com.lemnt.mall.domain.product.model.ProductSkuSpec(spec.id(), "接口颜色", option.id(), "红色")),
                catalogService.sku(Long.valueOf(savedId)).properties());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/mall/spus")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(payload.replace("\"valueId\":\"" + option.id() + "\"", "\"valueId\":0")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnprocessableEntity());
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/mall/spus")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(payload.replace("\"propertyId\":", "\"propertyName\":\"伪造名称\",\"propertyId\":")))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isBadRequest());
        assertEquals(1, count("mall_product_spu"));
        assertEquals(1, storedPropertyCount());
    }

    /** 从实际SKU JSON列核对规格数量，不依赖旧关系表。 */
    private long storedPropertyCount() throws Exception {
        long total = 0;
        try (var statement = schemaConnection.createStatement();
                var rows = statement.executeQuery("SELECT properties FROM mall_product_sku")) {
            while (rows.next()) {
                var json = rows.getString(1);
                if (json != null) { total += com.alibaba.fastjson2.JSON.parseArray(json).size(); }
            }
        }
        return total;
    }

    /** JSON同步失败时，字典名称与SKU规格在同一事务回滚。 */
    @Test
    void dictionaryRenameFailureRollsBackBothDictionaryAndSkuJson() throws Exception {
        var service = components.get(ProductSpecService.class);
        var spec = service.create(new ProductSpecCreateCmd("回滚颜色", null));
        var option = service.createValue(new ProductSpecValueCreateCmd(spec.id(), "红", null));
        var saved = catalogService.createSpu(identitySpu("RENAME-ROLLBACK", identitySku("ROLL-SKU", Map.of(spec.id(), option.id()))));
        var before = catalogService.sku(saved.skus().get(0).id());
        try (var statement = schemaConnection.createStatement()) {
            statement.execute("ALTER TABLE mall_product_sku ADD CONSTRAINT reject_property_rename CHECK(properties NOT LIKE '%禁止名称%')");
        }
        assertThrows(RuntimeException.class, () -> service.updateValue(new ProductSpecValueUpdateCmd(option.id(), "禁止名称", null)));
        assertEquals("红", components.get(ProductSpecValueGateway.class).find(option.id()).name());
        assertEquals(before, catalogService.sku(before.id()));
    }
}
