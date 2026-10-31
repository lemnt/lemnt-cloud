package com.lemnt.mall.start.product;

import com.lemnt.mall.application.promotion.cmd.DiscountActivityCreateCmd;
import com.lemnt.mall.application.promotion.cmd.DiscountActivityUpdateCmd;
import com.lemnt.mall.application.promotion.cmd.RewardActivityCreateCmd;
import com.lemnt.mall.application.promotion.service.ActivityManagementService;
import com.lemnt.mall.application.promotion.service.ActivityPricingService;
import com.lemnt.mall.domain.promotion.model.DiscountProduct;
import com.lemnt.mall.domain.promotion.model.RewardRule;
import com.lemnt.mall.domain.product.model.ProductSpu;
import com.lemnt.mall.domain.product.model.ProductSku;
import com.lemnt.mall.domain.coupon.model.Coupon;
import com.lemnt.mall.domain.coupon.model.CouponTemplate;
import com.lemnt.mall.domain.settlement.model.SettlementItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** 生产 Mapper/XML 与事务代理验证直接活动管理、SKU 折扣和商品整体保存。 */
class ActivityAlignmentPersistenceTest {
    CatalogPersistenceTest f;
    ActivityManagementService management;
    ActivityPricingService pricing;
    ProductSpu spu;
    LocalDateTime now = LocalDateTime.of(2026, 10, 28, 12, 0);

    @BeforeEach
    void setup() throws Exception {
        f = new CatalogPersistenceTest();
        f.setup();
        ScriptUtils.executeSqlScript(f.schemaConnection, new ClassPathResource("sql/V15__promotion_activity.sql"));
        management = f.components.get(ActivityManagementService.class);
        pricing = f.components.get(ActivityPricingService.class);
        spu = f.create("ACTIVITY", f.draft("RED", 10), f.draft("BLUE", 10));
    }

    @AfterEach
    void cleanup() throws Exception {
        f.closeSchemaConnection();
    }

    DiscountProduct discount() {
        return new DiscountProduct(null, null, spu.id(), spu.skus().get(0).id(), "PERCENT", 8000, null);
    }

    Long createDiscount() {
        return management.createDiscount(new DiscountActivityCreateCmd("SKU八折", now.minusDays(1),
                now.plusDays(1), null, List.of(discount())));
    }

    @Test
    void activityIsImmediatelyEnabledAndClosedActivityCannotBeEditedOrReopened() {
        var id = createDiscount();
        assertEquals(0, management.detailDiscount(id).status());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> management.deleteDiscount(id));
        management.closeDiscount(id);
        assertEquals(1, management.detailDiscount(id).status());
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> management.updateDiscount(
                new DiscountActivityUpdateCmd(id, "关闭后修改", now, now.plusDays(1), null, List.of(discount()))));
        assertTrue(management.deleteDiscount(id));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> management.detailDiscount(id));
    }

    @Test
    void sameSkuCannotJoinTwoOpenDiscountActivities() {
        createDiscount();
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, this::createDiscount);
    }

    @Test
    void rewardRejectsOverlappingScopeAndInvalidThresholds() {
        management.createReward(new RewardActivityCreateCmd("满40减4", now.minusDays(1), now.plusDays(1),
                null, "PRICE", "ALL", List.of(), List.of(new RewardRule(new BigDecimal("40.00"), new BigDecimal("4.00")))));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> management.createReward(
                new RewardActivityCreateCmd("重复范围", now, now.plusHours(2), null, "PRICE", "SPU_IDS",
                        List.of(spu.id()), List.of(new RewardRule(BigDecimal.TEN, BigDecimal.ONE)))));
    }

    @Test
    void skuDiscountThenRewardThenCouponPreservesActualLineAmounts() {
        createDiscount();
        management.createReward(new RewardActivityCreateCmd("满40减4", now.minusDays(1), now.plusDays(1),
                null, "PRICE", "ALL", List.of(), List.of(new RewardRule(new BigDecimal("40.00"), new BigDecimal("4.00")))));
        var red = spu.skus().get(0);
        var blue = spu.skus().get(1);
        var items = List.of(new SettlementItem(red.id(), red.code(), red.name(), red.price(), 3, new BigDecimal("30.00"),
                null, BigDecimal.ZERO, spu.id(), spu.name(), List.of(new SettlementItem.Property(null, "颜色", null, "红色")), "original.png"),
                new SettlementItem(blue.id(), blue.code(), blue.name(), blue.price(), 2, new BigDecimal("20.00")));
        var template = new CouponTemplate(2L, "C", "减3", "FIXED", BigDecimal.ZERO, new BigDecimal("3.00"),
                null, null, 100, 1, now.minusDays(1), now.plusDays(1), true);
        var coupon = new Coupon(1L, 2L, 7L, "AVAILABLE", null,
                com.lemnt.mall.domain.coupon.model.CouponRules.snapshot(template, now), null);
        var quote = pricing.price(items, coupon, now, 100);
        assertEquals(new BigDecimal("50.00"), quote.originalAmount());
        assertEquals(new BigDecimal("37.00"), quote.payableAmount());
        assertEquals(new BigDecimal("13.00"), quote.discountAmount());
        assertEquals(new BigDecimal("20.19"), quote.items().get(0).payAmount());
        assertEquals(spu.id(), quote.items().get(0).spuId());
        assertEquals(List.of(new SettlementItem.Property(null, "颜色", null, "红色")), quote.items().get(0).properties());
        assertEquals("original.png", quote.items().get(0).picUrl());
        assertEquals(quote.payableAmount(), quote.items().stream().map(SettlementItem::payAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        // 会员九折与 SKU 八折二选一，其他商品仍参与会员折扣。
        assertEquals(new BigDecimal("35.00"), pricing.price(items, coupon, now, 90).payableAmount());
    }

    @Test
    void spuSaveUpdatesSkuStockAndKeepsSkuIdentity() {
        var sku = spu.skus().get(0);
        var changed = new ProductSku(sku.id(), spu.id(), sku.code(), "新名称", new BigDecimal("12.00"),
                6, sku.image(), sku.properties(), sku.status());
        assertTrue(f.catalogService.updateSpu(new ProductSpu(spu.id(), spu.code(), "修改商品", spu.categoryId(),
                spu.brandId(), spu.description(), spu.image(), spu.status(), List.of(changed, spu.skus().get(1)))));
        var actual = f.catalogService.sku(sku.id());
        assertEquals(6, actual.stock());
        assertEquals(new BigDecimal("12.00"), actual.price());
        assertEquals("新名称", actual.name());
        assertEquals(2, f.productSkuGateway.catalogSkus(spu.id()).size());
    }

    @Test
    void fullSpuSaveCanSwapExistingSkuSpecifications() {
        var first = spu.skus().get(0);
        var second = spu.skus().get(1);
        var a = new ProductSku(first.id(), spu.id(), first.code(), first.name(), first.price(),
                first.stock(), first.image(), second.properties(), first.status());
        var b = new ProductSku(second.id(), spu.id(), second.code(), second.name(), second.price(),
                second.stock(), second.image(), first.properties(), second.status());
        assertTrue(f.catalogService.updateSpu(new ProductSpu(spu.id(), spu.code(), spu.name(), spu.categoryId(),
                spu.brandId(), spu.description(), spu.image(), spu.status(), List.of(a, b))));
        assertEquals(second.properties(), f.catalogService.sku(first.id()).properties());
        assertEquals(first.properties(), f.catalogService.sku(second.id()).properties());
    }

    @Test
    void refundInventoryReturnIsIdempotentAndBoundedByConfirmedQuantity() {
        var id = spu.skus().get(0).id();
        f.productSpuGateway.shelf(spu.id(), true);
        assertTrue(f.productInventoryService.reserveStock("ORDER", id, 3));
        f.productInventoryService.confirmStock("ORDER", id);
        f.productInventoryService.returnStock("ORDER", "REFUND1", id, 2);
        f.productInventoryService.returnStock("ORDER", "REFUND1", id, 2);
        assertEquals(9, f.productSkuGateway.catalogSku(id).stock());
        assertThrows(IllegalStateException.class, () -> f.productInventoryService.returnStock("ORDER", "REFUND2", id, 2));
        assertEquals(9, f.productSkuGateway.catalogSku(id).stock());
        f.productInventoryService.returnStock("ORDER", "REFUND2", id, 1);
        assertEquals(10, f.productSkuGateway.catalogSku(id).stock());
    }
    /** 积分按会员配置抵扣，赠送积分按最终实付计算并随结算单保存。 */
    @Test
    void pointDeductionAndRewardsUseConfirmedQuoteAndRejectZeroCashPayment() {
        var memberTrade = org.mockito.Mockito.mock(com.lemnt.mall.domain.member.gateway.MemberTradeGateway.class);
        org.mockito.Mockito.when(memberTrade.config()).thenReturn(
                new com.lemnt.mall.domain.member.model.MemberPointConfig(true, 1, 20, 2));
        var service = new ActivityPricingService(memberTrade,
                f.components.get(com.lemnt.mall.domain.promotion.gateway.DiscountActivityGateway.class),
                f.components.get(com.lemnt.mall.domain.promotion.gateway.DiscountProductGateway.class),
                f.components.get(com.lemnt.mall.domain.promotion.gateway.RewardActivityGateway.class),
                f.productSkuGateway, f.productSpuGateway);
        var sku = spu.skus().get(0);
        var items = List.of(new SettlementItem(sku.id(), sku.code(), sku.name(), sku.price(), 1, BigDecimal.TEN));
        var quote = service.price(items, null, now, 100, true, 50);
        assertEquals(20L, quote.usePoint());
        assertEquals(19L, quote.givePoint());
        assertEquals(new BigDecimal("9.80"), quote.payableAmount());
        org.mockito.Mockito.when(memberTrade.config()).thenReturn(
                new com.lemnt.mall.domain.member.model.MemberPointConfig(true, 1, 0, 2));
        assertThrows(com.lemnt.common.core.exception.ServiceException.class, () -> service.price(items, null, now, 100, true, 1000));
    }
    /** maxPrice历史字段实际表示积分数，单积分5分、最多2积分应只抵扣0.10元。 */
    @Test
    void pointCapIsPointCountAndDisabledSwitchAlsoStopsGift() {
        var memberTrade = org.mockito.Mockito.mock(com.lemnt.mall.domain.member.gateway.MemberTradeGateway.class);
        org.mockito.Mockito.when(memberTrade.config()).thenReturn(
                new com.lemnt.mall.domain.member.model.MemberPointConfig(true, 5, 2, 3));
        var service = new ActivityPricingService(memberTrade,
                f.components.get(com.lemnt.mall.domain.promotion.gateway.DiscountActivityGateway.class),
                f.components.get(com.lemnt.mall.domain.promotion.gateway.DiscountProductGateway.class),
                f.components.get(com.lemnt.mall.domain.promotion.gateway.RewardActivityGateway.class),
                f.productSkuGateway, f.productSpuGateway);
        var item = new SettlementItem(spu.skus().get(0).id(), "SKU", "商品", new BigDecimal("10.00"), 1,
                new BigDecimal("10.00"));
        var enabled = service.price(List.of(item), null, now, 100, true, 100L);
        assertEquals(2L, enabled.usePoint());
        assertEquals(new BigDecimal("9.90"), enabled.payableAmount());
        assertEquals(29L, enabled.givePoint());
        org.mockito.Mockito.when(memberTrade.config()).thenReturn(
                new com.lemnt.mall.domain.member.model.MemberPointConfig(false, 5, 2, 3));
        var disabled = service.price(List.of(item), null, now, 100, true, 100L);
        assertEquals(0L, disabled.usePoint());
        assertEquals(0L, disabled.givePoint());
        assertEquals(new BigDecimal("10.00"), disabled.payableAmount());
    }
}
