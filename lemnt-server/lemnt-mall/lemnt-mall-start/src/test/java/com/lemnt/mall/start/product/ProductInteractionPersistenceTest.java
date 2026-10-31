package com.lemnt.mall.start.product;

import com.lemnt.mall.application.product.service.ProductFavoriteService;
import com.lemnt.mall.application.product.service.ProductBrowseHistoryService;
import com.lemnt.mall.application.product.cmd.ProductFavoriteCreateCmd;
import com.lemnt.mall.application.product.cmd.ProductBrowseHistoryCreateCmd;
import com.lemnt.mall.application.product.qry.ProductFavoritePageQry;
import com.lemnt.mall.application.product.qry.ProductBrowseHistoryPageQry;
import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.context.SecurityContextHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** 生产DDL/Gateway/事务及基础锁模板；只替换Redis锁执行器传输。 */
class ProductInteractionPersistenceTest {
    CatalogPersistenceTest f;
    ProductFavoriteService favorites;
    ProductBrowseHistoryService history;
    Long spu;
    @BeforeEach
    void setup() throws Exception {
        f = new CatalogPersistenceTest(); f.setup();
        ScriptUtils.executeSqlScript(f.schemaConnection, new ClassPathResource("sql/V17__product_interaction.sql"));
        f.components.register(com.baomidou.lock.LockTemplate.class, memberLocks());
        favorites = f.components.get(ProductFavoriteService.class);
        history = f.components.get(ProductBrowseHistoryService.class);
        spu = f.create("INTERACTION", f.draft("红色", 3)).id();
    }
    @AfterEach
    void close() throws Exception { SecurityContextHolder.remove(); f.closeSchemaConnection(); }

    @Test
    void favoriteDuplicatesOwnershipAndRecreationUseRealUniqueRows() {
        var first = favorites.create(new ProductFavoriteCreateCmd(1L, spu));
        assertEquals(409, assertThrows(ServiceException.class, () -> favorites.create(new ProductFavoriteCreateCmd(1L, spu))).getCode());
        assertTrue(favorites.exists(1L, spu)); assertFalse(favorites.exists(2L, spu));
        assertThrows(ServiceException.class, () -> favorites.remove(2L, spu));
        var page = favorites.page(new ProductFavoritePageQry(1L, null, 1, 20));
        assertEquals(1, page.getTotal()); assertEquals("goods", page.getRecords().get(0).spuName());
        assertEquals(new java.math.BigDecimal("10.00"), page.getRecords().get(0).price());
        assertTrue(favorites.remove(1L, spu));
        assertNotEquals(first, favorites.create(new ProductFavoriteCreateCmd(1L, spu)));
        assertEquals(1, favorites.count(1L));
    }

    @Test
    void concurrentFavoritesAllowExactlyOneRecordAndReleaseLocks() throws Exception {
        var pool = Executors.newFixedThreadPool(6);
        try {
            var tasks = java.util.stream.IntStream.range(0, 6).mapToObj(i -> pool.submit(() -> {
                try { favorites.create(new ProductFavoriteCreateCmd(1L, spu)); return true; }
                catch (ServiceException conflict) { assertEquals(409, conflict.getCode()); return false; }
            })).toList();
            int successes = 0;
            for (var task : tasks) { if (task.get(10, TimeUnit.SECONDS)) { successes++; } }
            assertEquals(1, successes); assertEquals(1, favorites.count(1L));
            assertTrue(favorites.remove(1L, spu));
        } finally { pool.shutdownNow(); }
    }

    @Test
    void historyReordersHidesByOwnerAndRevisitRestoresVisibility() {
        history.create(new ProductBrowseHistoryCreateCmd(1L, spu));
        var first = history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 20)).getRecords().get(0);
        history.create(new ProductBrowseHistoryCreateCmd(1L, spu));
        assertNotEquals(first.id(), history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 20)).getRecords().get(0).id());
        history.create(new ProductBrowseHistoryCreateCmd(2L, spu));
        assertThrows(RuntimeException.class, () -> history.hide(1L, List.of()));
        history.hide(1L, List.of(spu));
        assertEquals(0, history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 20)).getTotal());
        assertEquals(1, history.page(new ProductBrowseHistoryPageQry(1L, null, true, 1, 20)).getTotal());
        assertEquals(1, history.page(new ProductBrowseHistoryPageQry(2L, null, false, 1, 20)).getTotal());
        history.create(new ProductBrowseHistoryCreateCmd(1L, spu));
        assertEquals(1, history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 20)).getTotal());
        history.clear(1L);
        assertEquals(1, history.page(new ProductBrowseHistoryPageQry(2L, null, false, 1, 20)).getTotal());
    }

    @Test
    void historyCapAndFailedReplacementRollBackPruning() throws Exception {
        for (long id = 1000; id < 1101; id++) { history.create(new ProductBrowseHistoryCreateCmd(1L, id)); }
        var page = history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 100));
        assertEquals(100, page.getTotal());
        assertEquals(1100L, page.getRecords().get(0).spuId());
        assertFalse(page.getRecords().stream().anyMatch(row -> row.spuId().equals(1000L)));
        assertNull(page.getRecords().get(0).spuName()); assertNull(page.getRecords().get(0).price());
        try (var statement = f.schemaConnection.createStatement()) {
            statement.execute("ALTER TABLE mall_product_browse_history ADD CONSTRAINT reject_history CHECK(spu_id<>9999)");
        }
        assertThrows(RuntimeException.class, () -> history.create(new ProductBrowseHistoryCreateCmd(1L, 9999L)));
        assertEquals(page, history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 100)));
    }

    @Test
    void concurrentHistoryNeverExceedsHundredRows() throws Exception {
        for (long id = 1000; id < 1090; id++) { history.create(new ProductBrowseHistoryCreateCmd(1L, id)); }
        var pool = Executors.newFixedThreadPool(8);
        try {
            var tasks = java.util.stream.LongStream.range(2000, 2030).mapToObj(id -> pool.submit(() -> history.create(new ProductBrowseHistoryCreateCmd(1L, id)))).toList();
            for (var task : tasks) { assertTrue(task.get(15, TimeUnit.SECONDS)); }
            assertEquals(100, history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 100)).getTotal());
        } finally { pool.shutdownNow(); }
    }

    @Test
    void realControllerIgnoresForgedIdentityAndKeepsHiddenRowsPrivate() throws Exception {
        SecurityContextHolder.setUserId("1");
        var mapperBuilder = tools.jackson.databind.json.JsonMapper.builder();
        new com.lemnt.common.start.config.JacksonConfig().customizer().customize(mapperBuilder);
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(
                f.components.get(com.lemnt.mall.adapter.app.product.AppProductFavoriteController.class),
                f.components.get(com.lemnt.mall.adapter.app.product.AppProductBrowseHistoryController.class))
                .setControllerAdvice(new com.lemnt.common.security.handler.GlobalExceptionHandler())
                .setMessageConverters(new org.springframework.http.converter.json.JacksonJsonHttpMessageConverter(mapperBuilder.build())).build();
        mvc.perform(post("/mall/app/favorites").contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"userId\":2,\"spuId\":" + spu + "}"))
                .andExpect(status().isOk());
        assertEquals(1, favorites.count(1L)); assertEquals(0, favorites.count(2L));
        mvc.perform(get("/mall/app/favorites").param("userId", "2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1)).andExpect(jsonPath("$.rows[0].userId").doesNotExist());
        mvc.perform(delete("/mall/app/browse-history").contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content("{\"spuIds\":[]}")).andExpect(status().is(422));
        history.create(new ProductBrowseHistoryCreateCmd(1L, spu)); history.clear(1L);
        mvc.perform(get("/mall/app/browse-history").param("userDeleted", "true")).andExpect(status().isOk()).andExpect(jsonPath("$.total").value(0));
        SecurityContextHolder.remove();
        mvc.perform(get("/mall/app/favorites")).andExpect(status().isUnauthorized());
    }
    private com.baomidou.lock.LockTemplate memberLocks() throws Exception {
        var locks = new java.util.concurrent.ConcurrentHashMap<String, java.util.concurrent.locks.ReentrantLock>();
        var executor = org.mockito.Mockito.mock(com.baomidou.lock.executor.RedissonLockExecutor.class);
        org.mockito.Mockito.when(executor.renewal()).thenReturn(true);
        org.mockito.Mockito.when(executor.acquire(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong())).thenAnswer(invocation -> {
            var lock = locks.computeIfAbsent(invocation.getArgument(0), key -> new java.util.concurrent.locks.ReentrantLock());
            if (!lock.tryLock(3, java.util.concurrent.TimeUnit.SECONDS)) {
                return null;
            }
            var instance = org.mockito.Mockito.mock(org.redisson.api.RLock.class);
            return instance;
        });
        // LockTemplate 调用执行器解锁；测试替身显式释放对应本地锁。
        org.mockito.Mockito.when(executor.releaseLock(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            var lock = locks.get(invocation.<String>getArgument(0));
            if (lock == null || !lock.isHeldByCurrentThread()) {
                return false;
            }
            lock.unlock();
            return true;
        });
        var properties = new com.baomidou.lock.spring.boot.autoconfigure.Lock4jProperties();
        properties.setPrimaryExecutor(com.baomidou.lock.executor.RedissonLockExecutor.class);
        var template = new com.baomidou.lock.LockTemplate();
        template.setProperties(properties);
        template.setExecutors(java.util.List.of(executor));
        template.afterPropertiesSet();
        return template;
    }

}
