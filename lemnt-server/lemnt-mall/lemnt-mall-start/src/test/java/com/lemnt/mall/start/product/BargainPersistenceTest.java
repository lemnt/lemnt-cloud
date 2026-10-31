package com.lemnt.mall.start.product;

import com.lemnt.mall.application.promotion.cmd.BargainActivityCreateCmd;
import com.lemnt.mall.application.promotion.service.BargainActivityManagementService;
import com.lemnt.mall.application.promotion.service.BargainRecordService;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.domain.promotion.gateway.BargainActivityGateway;
import com.lemnt.mall.domain.promotion.model.BargainActivity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/** 生产 Mapper/XML 与事务代理验证砍价发起、助力、成功和到期失败。 */
class BargainPersistenceTest {
    CatalogPersistenceTest f;
    BargainActivityManagementService activities;
    BargainRecordService records;
    ProductSpu spu;
    Long activityId;

    @BeforeEach
    void setup() throws Exception {
        f = new CatalogPersistenceTest();
        f.setup();
        ScriptUtils.executeSqlScript(f.schemaConnection, new ClassPathResource("sql/V20__promotion_bargain.sql"));
        activities = f.components.get(BargainActivityManagementService.class);
        records = f.components.get(BargainRecordService.class);
        spu = f.create("BARGAIN", f.draft("RED", 10));
        var now = LocalDateTime.now();
        activityId = activities.create(new BargainActivityCreateCmd("砍价", now.minusDays(1), now.plusDays(1),
                spu.id(), spu.skus().get(0).id(), new BigDecimal("10.00"), new BigDecimal("9.00"), 5, 2, 1,
                1, new BigDecimal("1.00"), new BigDecimal("1.00")));
    }

    @AfterEach
    void cleanup() throws Exception {
        f.closeSchemaConnection();
    }

    @Test
    void friendHelpCutsToMinimumAndCompletes() {
        var id = records.create(1L, activityId);
        var help = records.help(2L, id);
        assertEquals(new BigDecimal("1.00"), help.reducePrice());
        var detail = records.detail(id, 1L);
        assertEquals(2, detail.record().status());
        assertEquals(new BigDecimal("9.00"), detail.record().bargainPrice());
        assertEquals(1, detail.helps().size());
        assertFalse(detail.helped());
        assertTrue(records.detail(id, 2L).helped());
    }

    @Test
    void selfHelpDuplicateHelpAndSecondInProgressAreRejected() {
        var id = records.create(1L, activityId);
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> records.help(1L, id));
        records.help(2L, id);
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> records.help(2L, id));
        var other = f.create("BARGAIN2", f.draft("BLUE", 10));
        var second = activities.create(new BargainActivityCreateCmd("另一个砍价", LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1), other.id(), other.skus().get(0).id(),
                new BigDecimal("10.00"), new BigDecimal("8.00"), 5, 2, 1, 1,
                new BigDecimal("1.00"), new BigDecimal("1.00")));
        records.create(3L, second);
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> records.create(3L, second));
    }

    @Test
    void activityEndMarksInProgressRecordFailed() {
        var id = records.create(1L, activityId);
        var gateway = f.components.get(BargainActivityGateway.class);
        var current = gateway.find(activityId);
        gateway.save(new BargainActivity(current.id(), current.name(), current.startTime(),
                LocalDateTime.now().minusMinutes(1), current.status(), current.spuId(), current.skuId(),
                current.bargainFirstPrice(), current.bargainMinPrice(), current.stock(), current.totalStock(),
                current.helpMaxCount(), current.bargainCount(), current.totalLimitCount(),
                current.randomMinPrice(), current.randomMaxPrice()));
        assertEquals(1, records.expire());
        assertEquals(3, records.detail(id, 1L).record().status());
    }

    @Test
    void stockDecreaseAndRestoreAreBounded() {
        activities.decreaseStock(activityId, 2);
        assertEquals(3, activities.detail(activityId).stock());
        activities.increaseStock(activityId, 2);
        assertEquals(5, activities.detail(activityId).stock());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> activities.decreaseStock(activityId, 6));
    }
}
