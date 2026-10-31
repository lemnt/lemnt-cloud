package com.lemnt.mall.start.product;

import com.lemnt.mall.application.promotion.cmd.PointActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.PointActivityUpdateCmd;
import com.lemnt.mall.application.promotion.cmd.PointProductCmd;
import com.lemnt.mall.application.promotion.service.PointActivityManagementService;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.domain.promotion.gateway.PointProductGateway;
import com.lemnt.mall.domain.promotion.model.PointProduct;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 生产 Mapper/XML 与事务代理验证积分商城活动、兑换校验和库存。 */
class PointPersistenceTest {
    CatalogPersistenceTest f;
    PointActivityManagementService service;
    ProductSpu spu;
    Long activityId;

    @BeforeEach
    void setup() throws Exception {
        f = new CatalogPersistenceTest();
        f.setup();
        ScriptUtils.executeSqlScript(f.schemaConnection, new ClassPathResource("sql/V21__promotion_point.sql"));
        service = f.components.get(PointActivityManagementService.class);
        spu = f.create("POINT", f.draft("RED", 10), f.draft("BLUE", 10));
        activityId = service.create(new PointActivityCreateCmd(spu.id(), "积分兑换", 1,
                List.of(new PointProductCmd(spu.skus().get(0).id(), 3, 100, new BigDecimal("5.00"), 3),
                        new PointProductCmd(spu.skus().get(1).id(), 5, 50, BigDecimal.ZERO, 2))));
    }

    @AfterEach
    void cleanup() throws Exception {
        f.closeSchemaConnection();
    }

    @Test
    void sumsStockAndShowsLowestPointProduct() {
        var detail = service.detail(activityId);
        assertEquals(5, detail.stock());
        assertEquals(5, detail.totalStock());
        assertEquals(50, detail.point());
        assertEquals(0, BigDecimal.ZERO.compareTo(detail.price()));
        assertEquals(2, detail.products().size());
    }

    @Test
    void validateJoinChecksExchangeCountAndStock() {
        var red = spu.skus().get(0).id();
        var join = service.validateJoin(activityId, red, 3);
        assertEquals(100, join.point());
        assertEquals(new BigDecimal("5.00"), join.price());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> service.validateJoin(activityId, red, 4));
    }

    @Test
    void stockDecreaseAndRestoreUpdateBothTables() {
        var red = spu.skus().get(0).id();
        service.decreaseStock(activityId, red, 2);
        assertEquals(3, service.detail(activityId).stock());
        assertEquals(1, service.detail(activityId).products().stream()
                .filter(item -> item.skuId().equals(red)).findFirst().orElseThrow().stock());
        service.increaseStock(activityId, red, 2);
        assertEquals(5, service.detail(activityId).stock());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> service.decreaseStock(activityId, red, 4));
    }

    @Test
    void closedActivityCannotBeEditedAndProductSnapshotIsDisabled() {
        service.close(activityId);
        assertEquals(1, service.detail(activityId).status());
        var product = f.components.get(PointProductGateway.class).byActivityId(activityId);
        assertTrue(product.stream().allMatch(item -> item.activityStatus() == 1));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class,
                () -> service.update(new PointActivityUpdateCmd(activityId, spu.id(), null, 1,
                        List.of(new PointProductCmd(spu.skus().get(0).id(), 1, 10, BigDecimal.ONE, 1)))));
        assertTrue(service.delete(activityId));
    }
}
