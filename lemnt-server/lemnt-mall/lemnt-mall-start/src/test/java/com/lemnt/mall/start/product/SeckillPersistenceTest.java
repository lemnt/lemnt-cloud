package com.lemnt.mall.start.product;

import com.lemnt.mall.application.promotion.cmd.SeckillActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.SeckillActivityUpdateCmd;
import com.lemnt.mall.application.promotion.cmd.SeckillConfigCreateCmd;
import com.lemnt.mall.application.promotion.cmd.SeckillProductCmd;
import com.lemnt.mall.application.promotion.service.SeckillActivityManagementService;
import com.lemnt.mall.application.promotion.service.SeckillAppQueryService;
import com.lemnt.mall.application.promotion.service.SeckillConfigManagementService;
import com.lemnt.mall.application.promotion.service.SeckillStockService;
import com.lemnt.mall.domain.product.model.ProductSpu;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 生产 Mapper/XML 与事务代理验证秒杀时段冲突、活动库存和 App 查询。 */
class SeckillPersistenceTest {
    CatalogPersistenceTest f;
    SeckillConfigManagementService configs;
    SeckillActivityManagementService activities;
    SeckillAppQueryService app;
    SeckillStockService stock;
    ProductSpu spu;
    Long configId;

    @BeforeEach
    void setup() throws Exception {
        f = new CatalogPersistenceTest();
        f.setup();
        ScriptUtils.executeSqlScript(f.schemaConnection, new ClassPathResource("sql/V18__promotion_seckill.sql"));
        configs = f.components.get(SeckillConfigManagementService.class);
        activities = f.components.get(SeckillActivityManagementService.class);
        app = f.components.get(SeckillAppQueryService.class);
        stock = f.components.get(SeckillStockService.class);
        spu = f.create("SECKILL", f.draft("RED", 10), f.draft("BLUE", 10));
        configId = configs.create(new SeckillConfigCreateCmd("全天", "00:00:00", "23:59:59",
                List.of("https://example.test/banner.png"), 0));
    }

    @AfterEach
    void cleanup() throws Exception {
        f.closeSchemaConnection();
    }

    Long create() {
        var now = LocalDateTime.now();
        return activities.create(new SeckillActivityCreateCmd(spu.id(), "限时秒杀", null,
                now.minusDays(1), now.plusDays(1), 0, List.of(configId), 10, 5,
                List.of(new SeckillProductCmd(spu.skus().get(0).id(), new BigDecimal("5.00"), 3),
                        new SeckillProductCmd(spu.skus().get(1).id(), new BigDecimal("6.00"), 2))));
    }

    @Test
    void overlappingConfigIsRejectedAndReferencedConfigCannotBeDeleted() {
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> configs.create(new SeckillConfigCreateCmd("重叠", "12:00:00", "13:00:00", List.of(), 0)));
        create();
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> configs.delete(configId));
        assertEquals(1, configs.detail(configId).activityCount());
    }

    @Test
    void activityStockIsSummedAndClosedActivityCannotBeEdited() {
        var id = create();
        var detail = activities.detail(id);
        assertEquals(5, detail.stock());
        assertEquals(5, detail.totalStock());
        assertEquals(new BigDecimal("5.00"), detail.seckillPrice());
        activities.close(id);
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> activities.update(new SeckillActivityUpdateCmd(id, spu.id(), "修改", null,
                        detail.startTime(), detail.endTime(), 0, List.of(configId), 10, 5,
                        List.of(new SeckillProductCmd(spu.skus().get(0).id(), new BigDecimal("5.00"), 3)))));
        assertTrue(activities.delete(id));
    }

    @Test
    void sameSpuAndConfigCannotJoinTwoEnabledActivities() {
        create();
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, this::create);
    }

    @Test
    void joinValidatesLimitAndStockThenDecreasesAndRestoresBothTables() {
        var id = create();
        var skuId = spu.skus().get(0).id();
        var join = stock.validateJoin(id, skuId, 3);
        assertEquals(10, join.totalLimitCount());
        assertEquals(new BigDecimal("5.00"), join.seckillPrice());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> stock.validateJoin(id, skuId, 4));
        stock.decreaseStock(id, skuId, 2);
        assertEquals(3, activities.detail(id).stock());
        assertEquals(1, app.detail(id).products().stream()
                .filter(item -> item.skuId().equals(skuId)).findFirst().orElseThrow().stock());
        stock.increaseStock(id, skuId, 2);
        assertEquals(5, activities.detail(id).stock());
    }

    @Test
    void appDetailReturnsTodayTimesAndNowReturnsCurrentConfig() {
        var id = create();
        var detail = app.detail(id);
        assertEquals(spu.id(), detail.spuId());
        assertEquals(5, detail.stock());
        assertEquals(2, detail.products().size());
        var now = app.now();
        assertNotNull(now.config());
        assertTrue(now.activities().stream().anyMatch(item -> item.id().equals(id)));
    }

    @Test
    void restoringStockAfterActivityRemovalDoesNotBlockOrderCancellation() {
        var id = create();
        var skuId = spu.skus().get(0).id();
        activities.close(id);
        activities.delete(id);
        assertDoesNotThrow(() -> stock.increaseStock(id, skuId, 1));
    }
}
