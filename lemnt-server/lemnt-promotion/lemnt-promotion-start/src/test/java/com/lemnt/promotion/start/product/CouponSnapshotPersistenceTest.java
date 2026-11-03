package com.lemnt.promotion.start.product;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.promotion.application.coupon.cmd.CouponTemplateUpdateCmd;
import com.lemnt.promotion.application.coupon.executor.CouponManagementPageQueryExe;
import com.lemnt.promotion.application.coupon.executor.CouponRevokeCmdExe;
import com.lemnt.promotion.application.coupon.executor.CouponTemplateUpdateCmdExe;
import com.lemnt.promotion.application.coupon.qry.CouponManagementPageQry;
import com.lemnt.promotion.application.coupon.service.CouponIssuanceService;
import com.lemnt.promotion.application.coupon.service.CouponLifecycleService;
import com.lemnt.promotion.application.promotion.service.ActivityPricingService;
import com.lemnt.promotion.domain.coupon.gateway.CouponGateway;
import com.lemnt.promotion.domain.coupon.gateway.CouponTemplateGateway;
import com.lemnt.promotion.domain.coupon.model.Coupon;
import com.lemnt.promotion.domain.coupon.model.CouponOrderReference;
import com.lemnt.promotion.domain.coupon.model.CouponRules;
import com.lemnt.promotion.domain.coupon.model.CouponTemplate;
import com.lemnt.promotion.domain.coupon.model.CouponTerms;
import com.lemnt.promotion.domain.member.gateway.MemberLockGateway;
import com.lemnt.promotion.domain.settlement.model.SettlementItem;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        ScriptUtils.executeSqlScript(
                fixture.schemaConnection,
                new ClassPathResource("promotion-sql/lemnt_promotion.sql"));

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
        return templates.createTemplate(
                CouponRules.template(
                        new CouponTemplate(
                                null,
                                "TEST",
                                "领取时权益",
                                "FIXED",
                                BigDecimal.ZERO,
                                new BigDecimal("3.00"),
                                null,
                                null,
                                -1,
                                0,
                                now.minusDays(1),
                                now.plusDays(10),
                                true,
                                "测试说明",
                                "USER",
                                limit,
                                "SPU_IDS",
                                List.of(11L),
                                validity,
                                1,
                                3,
                                0)));
    }

    @Test
    void giftsTwoCouponsAndReplayDoesNotReissueRevokedCoupon() {
        var template = template(-1, "DATE");
        issuance.gift(7L, template.id(), "GIFT1", 1);
        issuance.gift(7L, template.id(), "GIFT1", 2);
        assertEquals(2, coupons.takeCount(7L, template.id()));
        assertEquals(2, templates.template(template.id()).issuedCount());
        var first = coupons.coupons(7L).get(0);
        assertEquals("GIFT1", first.sourceOrderNo());
        assertNotNull(first.giftSequence());
        assertTrue(coupons.revoke(7L, first.id()));
        issuance.gift(7L, template.id(), "GIFT1", 1);
        issuance.gift(7L, template.id(), "GIFT1", 2);
        assertEquals(1, coupons.takeCount(7L, template.id()));
        assertEquals(2, templates.template(template.id()).issuedCount());
        assertTrue(coupons.giftIssued("GIFT1", template.id(), 7L, 1));
        assertTrue(coupons.giftIssued("GIFT1", template.id(), 7L, 2));
    }

    @Test
    void finalGiftSequenceConstraintSupportsReplayWithoutDuplicatingCoupons() throws Exception {
        var template = template(-1, "DATE");
        var legacy = issuance.take(7L, template.id(), "USER");
        try (var statement = fixture.schemaConnection.createStatement()) {
            statement.execute(
                    "UPDATE promotion_coupon SET source_order_no='LEGACY',gift_sequence=1 WHERE id="
                            + legacy.id());
        }
        ScriptUtils.executeSqlScript(
                fixture.schemaConnection,
                new ClassPathResource("promotion-sql/lemnt_promotion.sql"));
        issuance.gift(7L, template.id(), "LEGACY", 1);
        issuance.gift(7L, template.id(), "LEGACY", 2);
        assertEquals(2, coupons.takeCount(7L, template.id()));
        assertEquals(2, templates.template(template.id()).issuedCount());
        assertEquals(1, coupons.coupon(7L, legacy.id(), false).giftSequence());
        var duplicate =
                new Coupon(
                        null,
                        template.id(),
                        7L,
                        "AVAILABLE",
                        null,
                        legacy.terms(),
                        null,
                        "LEGACY",
                        2);
        assertThrows(DuplicateKeyException.class, () -> coupons.insertCoupon(duplicate));
    }

    @Test
    void concurrentGiftCallbacksDoNotDuplicateCouponsOrQuota() throws Exception {
        var template = template(-1, "DATE");
        var pool = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            Callable<Void> callback =
                    () -> {
                        start.await();
                        issuance.gift(7L, template.id(), "CONCURRENT", 1);
                        issuance.gift(7L, template.id(), "CONCURRENT", 2);
                        return null;
                    };
            var first = pool.submit(callback);
            var second = pool.submit(callback);
            start.countDown();
            first.get(10, TimeUnit.SECONDS);
            second.get(10, TimeUnit.SECONDS);
            assertEquals(2, coupons.takeCount(7L, template.id()));
            assertEquals(2, templates.template(template.id()).issuedCount());
        } finally {
            pool.shutdownNow();
        }
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
        assertEquals(
                List.of(11L), coupons.coupon(7L, first.id(), false).terms().productScopeValues());
        assertEquals(
                new BigDecimal("3.00"),
                CouponRules.discount(first.terms(), BigDecimal.TEN, templates.now()));
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
        assertThrows(
                ServiceException.class,
                () -> CouponRules.discount(coupon.terms(), BigDecimal.TEN, before));
        issuance.take(7L, template.id(), "USER");
        assertThrows(ServiceException.class, () -> issuance.issue(7L, template.id()));
    }

    @Test
    void expiredLockedCouponIsNotUnlockedByExpiryAndRefundDoesNotReviveIt() throws Exception {
        var template = template(-1, "DATE");
        var coupon = issuance.take(7L, template.id(), "USER");
        coupons.lockCoupon(7L, coupon.id(), "ORDER1");
        try (var statement = fixture.schemaConnection.createStatement()) {
            statement.execute(
                    "UPDATE promotion_coupon SET valid_until=DATEADD('DAY',-1,CURRENT_TIMESTAMP) WHERE id="
                            + coupon.id());
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
        var terms =
                new CouponTerms(
                        "SCOPE",
                        "范围券",
                        "USER",
                        "FIXED",
                        new BigDecimal("15.00"),
                        new BigDecimal("5.00"),
                        null,
                        null,
                        "SPU_IDS",
                        List.of(first.id()),
                        now.minusDays(1),
                        now.plusDays(1));
        var coupon = new Coupon(1L, 1L, 7L, "AVAILABLE", null, terms, null);
        var items =
                List.of(
                        new SettlementItem(
                                firstSku,
                                "A",
                                "甲",
                                new BigDecimal("20.00"),
                                1,
                                new BigDecimal("20.00")),
                        new SettlementItem(
                                secondSku,
                                "B",
                                "乙",
                                new BigDecimal("40.00"),
                                1,
                                new BigDecimal("40.00")));
        var pricing = fixture.components.get(ActivityPricingService.class);
        try {
            ScriptUtils.executeSqlScript(
                    fixture.schemaConnection,
                    new ClassPathResource("promotion-sql/lemnt_promotion.sql"));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
        var quote = pricing.price(items, coupon, now, 100);
        assertEquals(new BigDecimal("15.00"), quote.items().get(0).payAmount());
        assertEquals(new BigDecimal("40.00"), quote.items().get(1).payAmount());
        assertEquals(new BigDecimal("55.00"), quote.payableAmount());
        assertThrows(
                ServiceException.class,
                () -> pricing.price(List.of(items.get(1)), coupon, now, 100));
    }

    @Test
    void concurrentClaimsCannotExceedPerMemberLimit() throws Exception {
        var template = template(1, "DATE");
        var executor = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            Callable<Boolean> task =
                    () -> {
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
            assertNotEquals(one.get(10, TimeUnit.SECONDS), two.get(10, TimeUnit.SECONDS));
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
        var lifecycle = fixture.components.get(CouponLifecycleService.class);
        coupons.lockCoupon(7L, coupon.id(), "COUNTER1");
        lifecycle.useCoupon("COUNTER1");
        lifecycle.useCoupon("COUNTER1");
        assertEquals(1, templates.template(template.id()).useCount());
        lifecycle.returnUsedCoupon("COUNTER1");
        lifecycle.returnUsedCoupon("COUNTER1");
        assertEquals(0, templates.template(template.id()).useCount());
        var revoke = fixture.components.get(CouponRevokeCmdExe.class);
        assertTrue(revoke.execute(7L, second.id()));
        assertEquals(1, templates.template(template.id()).issuedCount());
        assertThrows(ServiceException.class, () -> revoke.execute(7L, second.id()));
        assertEquals(1, templates.template(template.id()).issuedCount());
        var page =
                fixture.components
                        .get(CouponManagementPageQueryExe.class)
                        .execute(
                                new CouponManagementPageQry(7L, template.id(), "AVAILABLE", 1, 20));
        assertEquals(1, page.total());
        assertEquals(coupon.id(), page.records().get(0).id());
        assertNotNull(page.records().get(0).terms());
    }

    @Test
    void templateEditsChangeNewIssuanceButNeverRewriteOldTerms() {
        var template = template(-1, "DATE");
        var original = issuance.take(7L, template.id(), "USER");
        var update = fixture.components.get(CouponTemplateUpdateCmdExe.class);
        assertTrue(
                update.execute(
                        new CouponTemplateUpdateCmd(
                                template.id(),
                                "TEST",
                                "新规则",
                                "PERCENT",
                                BigDecimal.ZERO,
                                null,
                                80,
                                null,
                                -1,
                                null,
                                null,
                                "改为相对日期",
                                "USER",
                                -1,
                                "ALL",
                                List.of(),
                                "TERM",
                                0,
                                7)));
        var current = templates.template(template.id());
        assertNull(current.fixedAmount());
        assertNull(current.validFrom());
        assertEquals(1, current.issuedCount());
        var next = issuance.take(7L, template.id(), "USER");
        assertEquals("PERCENT", next.terms().kind());
        assertEquals("新规则", next.terms().name());
        assertEquals(
                new BigDecimal("3.00"),
                coupons.coupon(7L, original.id(), false).terms().fixedAmount());
        assertEquals("领取时权益", coupons.coupon(7L, original.id(), false).terms().name());
    }
}
