package com.lemnt.mall.domain.promotion;

import com.lemnt.mall.domain.promotion.model.DiscountRule;
import com.lemnt.mall.domain.promotion.model.PricingContext;
import com.lemnt.mall.domain.promotion.model.PricingException;
import com.lemnt.mall.domain.promotion.model.PricingLine;
import com.lemnt.mall.domain.promotion.model.PromotionScope;
import com.lemnt.mall.domain.promotion.model.PromotionVersion;
import com.lemnt.mall.domain.promotion.model.SelectedCoupon;
import com.lemnt.mall.domain.promotion.service.PromotionPricingService;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reads the design's canonical expected values directly; never regenerates them from the engine.
 */
class PromotionVectorsTest {
    @TestFactory
    Stream<DynamicTest> canonicalVectors() throws Exception {
        JsonNode root;
        try (var stream = getClass().getResourceAsStream("/promotion/pricing-vectors.json")) {
            assertNotNull(stream);
            root = JsonMapper.builder().build().readTree(stream);
        }
        assertEquals(
                PromotionPricingService.ALGORITHM_VERSION, root.get("algorithmVersion").asString());
        var tests = new ArrayList<DynamicTest>();
        for (var vector : root.get("vectors")) {
            tests.add(
                    DynamicTest.dynamicTest(
                    vector.get("id").asString(),
                    () -> {
                var expected = vector.get("expected");
                if (expected.has("errorCode")) {
                    var error =
                    assertThrows(
                            PricingException.class,
                            () ->
                            new PromotionPricingService()
                            .price(
                            context(
                            vector.get(
                            "input"))));
                    assertEquals(
                            expected.get("errorCode").asString(),
                            error.code().name());
                    return ;
                }
                var result =
                new PromotionPricingService()
                    .price(context(vector.get("input")));
                assertEquals(number(expected, "originalFen"), result.originalFen());
                assertEquals(
                        number(expected, "campaignDiscountFen"),
                        result.campaignDiscountFen());
                assertEquals(
                        number(expected, "couponDiscountFen"),
                        result.couponDiscountFen());
                assertEquals(number(expected, "payableFen"), result.payableFen());
                assertEquals(
                        expected.get("submittable").asBoolean(),
                        result.submittable());
                assertEquals(
                        expected.get("selectedCampaignId").isNull()
                        ? null
                        : number(expected, "selectedCampaignId"),
                        result.selectedCampaign() == null
                        ? null
                        : result.selectedCampaign().id());
                assertEquals(expected.get("lines").size(), result.lines().size());
                for (int i = 0; i < result.lines().size(); i++) {
                    var line = result.lines().get(i);
                    var target = expected.get("lines").get(i);
                    assertEquals(number(target, "productId"), line.productId());
                    assertEquals(
                            number(target, "campaignDiscountFen"),
                            line.campaignDiscountFen());
                    assertEquals(
                            number(target, "couponDiscountFen"),
                            line.couponDiscountFen());
                    assertEquals(number(target, "payableFen"), line.payableFen());
                }
            }));
        }
        assertEquals(23, tests.size());
        return tests.stream();
    }

    private static PricingContext context(JsonNode input) {
        var lines = new ArrayList<PricingLine>();
        for (var line : input.get("lines")) {
            lines.add(
                    new PricingLine(
                    number(line, "productId"),
                    number(line, "spuId"),
                    number(line, "unitPriceFen"),
                    line.get("quantity").asInt()));
        }
        var campaigns = new ArrayList<PromotionVersion>();
        for (var campaign : input.get("campaigns")) {
            var scope = campaign.get("scope");
            var ids = new ArrayList<Long>();
            for (var id : scope.get("ids")) {
                ids.add(Long.parseLong(id.asString()));
            }
            campaigns.add(
                    new PromotionVersion(
                    number(campaign, "id"),
                    campaign.get("version").asInt(),
                    campaign.get("priority").asInt(),
                    PromotionVersion.CouponPolicy.valueOf(
                    campaign.get("couponPolicy").asString()),
                    new PromotionScope(
                    PromotionScope.Kind.valueOf(scope.get("kind").asString()), ids),
                    rule(campaign.get("type").asString(), campaign.get("rule")),
                    1,
                    100));
        }
        var coupon = input.get("coupon");
        return new PricingContext(
                lines,
                campaigns,
                coupon.isNull()
                ? null
                : new SelectedCoupon(
                number(coupon, "id"), rule(coupon.get("kind").asString(), coupon)),
                PricingContext.Mode.valueOf(input.get("promotionMode").asString()),
                10);
    }

    private static DiscountRule rule(String kind, JsonNode node) {
        return switch (kind) {
            case "FULL_REDUCTION" -> {
                var tiers = new ArrayList<DiscountRule.Tier>();
                for (var tier : node.get("tiers")) {
                    tiers.add(
                            new DiscountRule.Tier(
                            number(tier, "thresholdFen"), number(tier, "discountFen")));
                }
                yield new DiscountRule.FullReduction(tiers);
            }
            case "FIXED" ->
            new DiscountRule.Fixed(
                    number(node, "thresholdFen"), number(node, "fixedAmountFen"));
            case "PERCENT", "ORDER_DISCOUNT" ->
            new DiscountRule.Percentage(
                    number(node, "thresholdFen"),
                    node.get("payRateBps").asInt(),
                    node.get("maxDiscountFen").isNull()
                    ? null
                    : number(node, "maxDiscountFen"));
            default -> throw new AssertionError("Unsupported fixture rule: " + kind);
        };
    }

    private static long number(JsonNode node, String field) {
        return Long.parseLong(node.get(field).asString());
    }
}
