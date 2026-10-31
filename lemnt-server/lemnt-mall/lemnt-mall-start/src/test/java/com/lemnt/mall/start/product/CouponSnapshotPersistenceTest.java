package com.lemnt.mall.start.product;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.coupon.service.CouponIssuanceService;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.mall.domain.coupon.model.CouponOrderReference;
import com.lemnt.mall.domain.coupon.model.CouponRules;
import com.lemnt.mall.domain.coupon.model.CouponTemplate;
import com.lemnt.mall.domain.member.gateway.MemberLockGateway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** 生产 SQL、Mapper 和事务代理验证冻结规则及领取限制，会员锁传输使用测试替身。 */
class CouponSnapshotPersistenceTest {
    CatalogPersistenceTest fixture;
    CouponGateway coupons;
    CouponTemplateGateway templates;
    CouponIssuanceService issuance;

    @BeforeEach
    void setup() throws Exception {
        fixture = new CatalogPersistenceTest();
        fixture.setup();
        ScriptUtils.executeSqlScript(fixture.schemaConnection, new ClassPathResource("sql/V10__cart_coupon_checkout.sql"));
        fixture.components.register(MemberLockGateway.class, memberId -> {});
        coupons = fixture.components.get(CouponGateway.class);
        templates = fixture.components.get(CouponTemplateGateway.class);
        issuance = fixture.components.get(CouponIssuanceService.class);
    }

    @AfterEach
    void close() throws Exception {
        fixture.closeSchemaConnection();
    }

    CouponTemplate template(int limit, String validity) {
        var now = templates.now();
        return templates.createTemplate(CouponRules.template(new CouponTemplate(null, "TEST", "领取时权益", "FIXED",
                BigDecimal.ZERO, new BigDecimal("3.00"), null, null, -1, 0,
                now.minusDays(1), now.plusDays(10), true, "测试说明", "USER", limit,
                "SPU_IDS", List.of(11L), validity, 1, 3, 0)));
    }

    @Test
    void multipleClaimsHaveIndependentFrozenRulesAndEnforceLimit() {
        var template = template(2, "DATE");
        var first = issuance.take(7L, template.id(), "USER");
        var second = issuance.take(7L, template.id(), "USER");
        assertNotEquals(first.id(), second.id());
        assertEquals(2, coupons.takeCount(7L, template.id()));
        assertEquals(2, templates.template(template.id()).issuedCount());
        assertThrows(ServiceException.class, () -> issuance.take(7L, template.id(), "USER"));
        templates.enableTemplate(template.id(), false);
        assertEquals(List.of(11L), coupons.coupon(7L, first.id(), false).terms().productScopeValues());
        assertEquals(new BigDecimal("3.00"), CouponRules.discount(first.terms(), BigDecimal.TEN, templates.now()));
        assertTrue(templates.deleteTemplate(template.id()));
        assertEquals("领取时权益", coupons.coupon(7L, second.id(), false).terms().name());
    }

    @Test
    void relativeValidityStartsAtIssuanceAndCanBeUnlimitedPerMember() {
        var template = template(-1, "TERM");
        var before = templates.now();
        var coupon = issuance.take(7L, template.id(), "USER");
        assertFalse(coupon.terms().validFrom().isBefore(before.plusDays(1).minusSeconds(1)));
        assertEquals(coupon.terms().validFrom().plusDays(3), coupon.terms().validUntil());
        assertThrows(ServiceException.class, () -> CouponRules.discount(coupon.terms(), BigDecimal.TEN, before));
        issuance.take(7L, template.id(), "USER");
        assertThrows(ServiceException.class, () -> issuance.issue(7L, template.id()));
    }

    @Test
    void expiredLockedCouponIsNotUnlockedByExpiryAndRefundDoesNotReviveIt() throws Exception {
        var template = template(-1, "DATE");
        var coupon = issuance.take(7L, template.id(), "USER");
        coupons.lockCoupon(7L, coupon.id(), "ORDER1");
        try (var statement = fixture.schemaConnection.createStatement()) {
            statement.execute("UPDATE mall_coupon SET valid_until=DATEADD('DAY',-1,CURRENT_TIMESTAMP) WHERE id=" + coupon.id());
        }
        assertEquals(0, coupons.expire(templates.now()));
        var reference = new CouponOrderReference(7L, coupon.id(), "ORDER1");
        assertTrue(coupons.useCoupon(reference));
        assertFalse(coupons.useCoupon(reference));
        assertNotNull(coupons.coupon(7L, coupon.id(), false).useTime());
        assertTrue(coupons.returnUsedCoupon("ORDER1"));
        assertFalse(coupons.returnUsedCoupon("ORDER1"));
        var returned = coupons.coupon(7L, coupon.id(), false);
        assertEquals("EXPIRED", returned.status());
        assertNull(returned.useTime());
        assertTrue(coupons.revoke(7L, coupon.id()));
        assertNull(coupons.coupon(7L, coupon.id(), false));
    }

    @Test
    void scopedCouponThresholdAndAllocationOnlyIncludeEligibleSpu() {
        var first = fixture.create("SCOPE_A", fixture.draft("RED", 10));
        var second = fixture.create("SCOPE_B", fixture.draft("BLUE", 10));
        var firstSku = first.skus().get(0).id();
        var secondSku = second.skus().get(0).id();
        var now = templates.now();
        var terms = new com.lemnt.mall.domain.coupon.model.CouponTerms("SCOPE", "范围券", "USER", "FIXED",
                new BigDecimal("15.00"), new BigDecimal("5.00"), null, null, "SPU_IDS", List.of(first.id()),
                now.minusDays(1), now.plusDays(1));
        var coupon = new com.lemnt.mall.domain.coupon.model.Coupon(1L, 1L, 7L, "AVAILABLE", null, terms, null);
        var items = List.of(new com.lemnt.mall.domain.settlement.model.SettlementItem(firstSku, "A", "甲", new BigDecimal("20.00"), 1, new BigDecimal("20.00")),
                new com.lemnt.mall.domain.settlement.model.SettlementItem(secondSku, "B", "乙", new BigDecimal("40.00"), 1, new BigDecimal("40.00")));
        var pricing = fixture.components.get(com.lemnt.mall.application.promotion.service.ActivityPricingService.class);
        try {
            ScriptUtils.executeSqlScript(fixture.schemaConnection, new ClassPathResource("sql/V15__promotion_activity.sql"));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
        var quote = pricing.price(items, coupon, now, 100);
        assertEquals(new BigDecimal("15.00"), quote.items().get(0).payAmount());
        assertEquals(new BigDecimal("40.00"), quote.items().get(1).payAmount());
        assertEquals(new BigDecimal("55.00"), quote.payableAmount());
        assertThrows(ServiceException.class, () -> pricing.price(List.of(items.get(1)), coupon, now, 100));
    }

    @Test
    void concurrentClaimsCannotExceedPerMemberLimit() throws Exception {
        var template = template(1, "DATE");
        var executor = java.util.concurrent.Executors.newFixedThreadPool(2);
        var start = new java.util.concurrent.CountDownLatch(1);
        try {
            java.util.concurrent.Callable<Boolean> task = () -> {
                start.await();
                try {
                    issuance.take(7L, template.id(), "USER");
                    return true;
                } catch (ServiceException exception) {
                    return false;
                }
            };
            var one = executor.submit(task);
            var two = executor.submit(task);
            start.countDown();
            assertNotEquals(one.get(10, java.util.concurrent.TimeUnit.SECONDS), two.get(10, java.util.concurrent.TimeUnit.SECONDS));
            assertEquals(1, coupons.takeCount(7L, template.id()));
            assertEquals(1, templates.template(template.id()).issuedCount());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void lifecycleCountersAndRevocationOnlyChangeOnce() {
        var template = template(2, "DATE");
        var coupon = issuance.take(7L, template.id(), "USER");
        var second = issuance.take(7L, template.id(), "USER");
        fixture.components.register(com.lemnt.mall.domain.settlement.gateway.SettlementGateway.class,
                org.mockito.Mockito.mock(com.lemnt.mall.domain.settlement.gateway.SettlementGateway.class));
        var lifecycle = fixture.components.get(com.lemnt.mall.application.settlement.service.SettlementRecordService.class);
        coupons.lockCoupon(7L, coupon.id(), "COUNTER1");
        lifecycle.useCoupon("COUNTER1");
        lifecycle.useCoupon("COUNTER1");
        assertEquals(1, templates.template(template.id()).useCount());
        lifecycle.returnUsedCoupon("COUNTER1");
        lifecycle.returnUsedCoupon("COUNTER1");
        assertEquals(0, templates.template(template.id()).useCount());
        var revoke = fixture.components.get(com.lemnt.mall.application.coupon.executor.CouponRevokeCmdExe.class);
        assertTrue(revoke.execute(7L, second.id()));
        assertEquals(1, templates.template(template.id()).issuedCount());
        assertThrows(ServiceException.class, () -> revoke.execute(7L, second.id()));
        assertEquals(1, templates.template(template.id()).issuedCount());
        var page = fixture.components.get(com.lemnt.mall.application.coupon.executor.CouponManagementPageQueryExe.class)
                .execute(new com.lemnt.mall.application.coupon.qry.CouponManagementPageQry(7L, template.id(), "AVAILABLE", 1, 20));
        assertEquals(1, page.total());
        assertEquals(coupon.id(), page.records().get(0).id());
        assertNotNull(page.records().get(0).terms());
    }

    @Test
    void templateEditsChangeNewIssuanceButNeverRewriteOldTerms() {
        var template = template(-1, "DATE");
        var original = issuance.take(7L, template.id(), "USER");
        var update = fixture.components.get(com.lemnt.mall.application.coupon.executor.CouponTemplateUpdateCmdExe.class);
        assertTrue(update.execute(new com.lemnt.mall.application.coupon.cmd.CouponTemplateUpdateCmd(template.id(), "TEST", "新规则", "PERCENT",
                BigDecimal.ZERO, null, 80, null, -1, null, null, "改为相对日期", "USER", -1, "ALL", List.of(), "TERM", 0, 7)));
        var current = templates.template(template.id());
        assertNull(current.fixedAmount());
        assertNull(current.validFrom());
        assertEquals(1, current.issuedCount());
        var next = issuance.take(7L, template.id(), "USER");
        assertEquals("PERCENT", next.terms().kind());
        assertEquals("新规则", next.terms().name());
        assertEquals(new BigDecimal("3.00"), coupons.coupon(7L, original.id(), false).terms().fixedAmount());
        assertEquals("领取时权益", coupons.coupon(7L, original.id(), false).terms().name());
    }
}
