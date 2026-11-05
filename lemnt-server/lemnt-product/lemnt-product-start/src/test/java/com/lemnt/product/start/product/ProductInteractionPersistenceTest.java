package com.lemnt.product.start.product;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.common.security.context.SecurityContextHolder;
import com.lemnt.common.security.handler.GlobalExceptionHandler;
import com.lemnt.common.start.config.JacksonConfig;
import com.lemnt.product.adapter.app.product.AppProductBrowseHistoryController;
import com.lemnt.product.adapter.app.product.AppProductFavoriteController;
import com.lemnt.product.application.product.cmd.ProductBrowseHistoryCreateCmd;
import com.lemnt.product.application.product.cmd.ProductFavoriteCreateCmd;
import com.lemnt.product.application.product.qry.ProductBrowseHistoryPageQry;
import com.lemnt.product.application.product.qry.ProductFavoritePageQry;
import com.lemnt.product.application.product.service.ProductBrowseHistoryService;
import com.lemnt.product.application.product.service.ProductFavoriteService;

import com.baomidou.lock.LockTemplate;
import com.baomidou.lock.executor.RedissonLockExecutor;
import com.baomidou.lock.spring.boot.autoconfigure.Lock4jProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.redisson.api.RLock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 生产DDL/Gateway/事务及基础锁模板；只替换Redis锁执行器传输。
 */
class ProductInteractionPersistenceTest {
    CatalogPersistenceTest f;
    ProductFavoriteService favorites;
    ProductBrowseHistoryService history;
    Long spu;

    @BeforeEach
    void setup() throws Exception {
        f = new CatalogPersistenceTest();
        f.setup();
        ScriptUtils.executeSqlScript(
            f.schemaConnection, new ClassPathResource("product-sql/lemnt_product.sql"));
        f.components.register(LockTemplate.class, memberLocks());
        favorites = f.components.get(ProductFavoriteService.class);
        history = f.components.get(ProductBrowseHistoryService.class);
        spu = f.create("INTERACTION", f.draft("红色", 3)).id();
    }

    @AfterEach
    void close() throws Exception {
        SecurityContextHolder.remove();
        f.closeSchemaConnection();
    }

    @Test
    void favoriteDuplicatesOwnershipAndRecreationUseRealUniqueRows() {
        var first = favorites.create(new ProductFavoriteCreateCmd(1L, spu));
        assertEquals(
            409,
            assertThrows(
                ServiceException.class,
                () -> favorites.create(new ProductFavoriteCreateCmd(1L, spu)))
                .getCode());
        assertTrue(favorites.exists(1L, spu));
        assertFalse(favorites.exists(2L, spu));
        assertThrows(ServiceException.class, () -> favorites.remove(2L, spu));
        var page = favorites.page(new ProductFavoritePageQry(1L, null, 1, 20));
        assertEquals(1, page.getTotal());
        assertEquals("goods", page.getRecords().get(0).spuName());
        assertEquals(new BigDecimal("10.00"), page.getRecords().get(0).price());
        assertTrue(favorites.remove(1L, spu));
        assertNotEquals(first, favorites.create(new ProductFavoriteCreateCmd(1L, spu)));
        assertEquals(1, favorites.count(1L));
    }

    @Test
    void concurrentFavoritesAllowExactlyOneRecordAndReleaseLocks() throws Exception {
        var pool = Executors.newFixedThreadPool(6);
        try {
            var tasks =
                IntStream.range(0, 6)
                    .mapToObj(
                        i ->
                            pool.submit(
                                () -> {
                                    try {
                                        favorites.create(
                                            new ProductFavoriteCreateCmd(
                                                1L, spu));
                                        return true;
                                    } catch (ServiceException conflict) {
                                        assertEquals(409, conflict.getCode());
                                        return false;
                                    }
                                }))
                    .toList();
            int successes = 0;
            for (var task : tasks) {
                if (task.get(10, TimeUnit.SECONDS)) {
                    successes++;
                }
            }
            assertEquals(1, successes);
            assertEquals(1, favorites.count(1L));
            assertTrue(favorites.remove(1L, spu));
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void historyReordersHidesByOwnerAndRevisitRestoresVisibility() {
        history.create(new ProductBrowseHistoryCreateCmd(1L, spu));
        var first =
            history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 20))
                .getRecords()
                .get(0);
        history.create(new ProductBrowseHistoryCreateCmd(1L, spu));
        assertNotEquals(
            first.id(),
            history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 20))
                .getRecords()
                .get(0)
                .id());
        history.create(new ProductBrowseHistoryCreateCmd(2L, spu));
        assertThrows(RuntimeException.class, () -> history.hide(1L, List.of()));
        history.hide(1L, List.of(spu));
        assertEquals(
            0,
            history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 20)).getTotal());
        assertEquals(
            1, history.page(new ProductBrowseHistoryPageQry(1L, null, true, 1, 20)).getTotal());
        assertEquals(
            1,
            history.page(new ProductBrowseHistoryPageQry(2L, null, false, 1, 20)).getTotal());
        history.create(new ProductBrowseHistoryCreateCmd(1L, spu));
        assertEquals(
            1,
            history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 20)).getTotal());
        history.clear(1L);
        assertEquals(
            1,
            history.page(new ProductBrowseHistoryPageQry(2L, null, false, 1, 20)).getTotal());
    }

    @Test
    void historyCapAndFailedReplacementRollBackPruning() throws Exception {
        for (long id = 1000; id < 1101; id++) {
            history.create(new ProductBrowseHistoryCreateCmd(1L, id));
        }
        var page = history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 100));
        assertEquals(100, page.getTotal());
        assertEquals(1100L, page.getRecords().get(0).spuId());
        assertFalse(page.getRecords().stream().anyMatch(row -> row.spuId().equals(1000L)));
        assertNull(page.getRecords().get(0).spuName());
        assertNull(page.getRecords().get(0).price());
        try (var statement = f.schemaConnection.createStatement()) {
            statement.execute(
                "ALTER TABLE product_browse_history ADD CONSTRAINT reject_history CHECK(spu_id<>9999)");
        }
        assertThrows(
            RuntimeException.class,
            () -> history.create(new ProductBrowseHistoryCreateCmd(1L, 9999L)));
        assertEquals(page, history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 100)));
    }

    @Test
    void concurrentHistoryNeverExceedsHundredRows() throws Exception {
        for (long id = 1000; id < 1090; id++) {
            history.create(new ProductBrowseHistoryCreateCmd(1L, id));
        }
        var pool = Executors.newFixedThreadPool(8);
        try {
            var tasks =
                LongStream.range(2000, 2030)
                    .mapToObj(
                        id ->
                            pool.submit(
                                () ->
                                    history.create(
                                        new ProductBrowseHistoryCreateCmd(
                                            1L, id))))
                    .toList();
            for (var task : tasks) {
                assertTrue(task.get(15, TimeUnit.SECONDS));
            }
            assertEquals(
                100,
                history.page(new ProductBrowseHistoryPageQry(1L, null, false, 1, 100))
                    .getTotal());
        } finally {
            pool.shutdownNow();
        }
    }

    @Test
    void realControllerIgnoresForgedIdentityAndKeepsHiddenRowsPrivate() throws Exception {
        SecurityContextHolder.setUserId("1");
        var mapperBuilder = JsonMapper.builder();
        new JacksonConfig().customizer().customize(mapperBuilder);
        var mvc =
            MockMvcBuilders.standaloneSetup(
                    f.components.get(AppProductFavoriteController.class),
                    f.components.get(AppProductBrowseHistoryController.class))
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(
                    new JacksonJsonHttpMessageConverter(mapperBuilder.build()))
                .build();
        mvc.perform(
                post("/app/favorites")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"userId\":2,\"spuId\":" + spu + "}"))
            .andExpect(status().isOk());
        assertEquals(1, favorites.count(1L));
        assertEquals(0, favorites.count(2L));
        mvc.perform(get("/app/favorites").param("userId", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(1))
            .andExpect(jsonPath("$.rows[0].userId").doesNotExist());
        mvc.perform(
                delete("/app/browse-historys")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"spuIds\":[]}"))
            .andExpect(status().is(422));
        history.create(new ProductBrowseHistoryCreateCmd(1L, spu));
        history.clear(1L);
        mvc.perform(get("/app/browse-historys").param("userDeleted", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.total").value(0));
        SecurityContextHolder.remove();
        mvc.perform(get("/app/favorites")).andExpect(status().isUnauthorized());
    }

    private LockTemplate memberLocks() throws Exception {
        var locks = new ConcurrentHashMap<String, ReentrantLock>();
        var executor = Mockito.mock(RedissonLockExecutor.class);
        Mockito.when(executor.renewal()).thenReturn(true);
        Mockito.when(
                executor.acquire(
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyLong(),
                    ArgumentMatchers.anyLong()))
            .thenAnswer(
                invocation -> {
                    var lock =
                        locks.computeIfAbsent(
                            invocation.getArgument(0), key -> new ReentrantLock());
                    if (!lock.tryLock(3, TimeUnit.SECONDS)) {
                        return null;
                    }
                    var instance = Mockito.mock(RLock.class);
                    return instance;
                });
        // LockTemplate 调用执行器解锁；测试替身显式释放对应本地锁。
        Mockito.when(
                executor.releaseLock(
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.anyString(),
                    ArgumentMatchers.any()))
            .thenAnswer(
                invocation -> {
                    var lock = locks.get(invocation.<String>getArgument(0));
                    if (lock == null || !lock.isHeldByCurrentThread()) {
                        return false;
                    }
                    lock.unlock();
                    return true;
                });
        var properties = new Lock4jProperties();
        properties.setPrimaryExecutor(RedissonLockExecutor.class);
        var template = new LockTemplate();
        template.setProperties(properties);
        template.setExecutors(List.of(executor));
        template.afterPropertiesSet();
        return template;
    }
}
