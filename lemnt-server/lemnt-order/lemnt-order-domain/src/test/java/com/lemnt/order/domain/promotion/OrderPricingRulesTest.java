package com.lemnt.order.domain.promotion;

import com.lemnt.order.domain.order.model.*;
import com.lemnt.order.domain.promotion.model.*;
import com.lemnt.order.domain.promotion.service.OrderPricingRules;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/** 可信内部报价仍需验证头、行、优惠及权益一致，不能接受缺字段的完整标记。 */
class OrderPricingRulesTest {
    BigDecimal n(String value) {
        return new BigDecimal(value);
    }

    TradeOrder order(String discount) {
        var item =
                new TradeOrderItem(
                        1L,
                        "SKU",
                        "商品",
                        n("6.00"),
                        2,
                        n("12.00"),
                        n("10.00"),
                        n("2.00"),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        n("2.00"),
                        n("3.00"),
                        n("1.00"),
                        n("-2.00"),
                        5L,
                        20L,
                        0);
        var point =
                new OrderPromotion(
                        null,
                        null,
                        "POINT",
                        "POINT_DEDUCT",
                        null,
                        "积分抵扣",
                        null,
                        "{}",
                        n("2.00"),
                        n("0.00"),
                        5L,
                        0L,
                        Map.of(),
                        List.of(
                                new OrderItemPromotion(
                                        null, null, null, "0", n("2.00"), n("0.00"), 5L, 0L)));
        var gift =
                new OrderPromotion(
                        null,
                        null,
                        "GIFT",
                        "ORDER_GIFT",
                        null,
                        "支付赠积分",
                        null,
                        "{\"givePerYuan\":2}",
                        n("0.00"),
                        n("0.00"),
                        0L,
                        20L,
                        Map.of(),
                        List.of(
                                new OrderItemPromotion(
                                        null, null, null, "0", n("0.00"), n("0.00"), 0L, 20L)));
        var freight =
                new OrderPromotion(
                        null,
                        null,
                        "FREE",
                        "REWARD",
                        1L,
                        "包邮",
                        null,
                        "{}",
                        n("0.00"),
                        n("1.00"),
                        0L,
                        0L,
                        Map.of(),
                        List.of(
                                new OrderItemPromotion(
                                        null, null, null, "0", n("0.00"), n("1.00"), 0L, 0L)));
        return new TradeOrder(
                null,
                "T1",
                1L,
                n("10.00"),
                "RESERVING",
                List.of(item),
                null,
                5L,
                20L,
                null,
                null,
                null,
                Map.of(),
                n("12.00"),
                n(discount),
                n("3.00"),
                n("1.00"),
                n("-2.00"),
                "COMPLETE",
                1,
                List.of(point, gift, freight));
    }

    @Test
    void acceptConsistentMoneyAndSeparatePointDimensions() {
        assertDoesNotThrow(() -> OrderPricingRules.validate(order("2.00")));
    }

    @Test
    void rejectHeaderLineMismatch() {
        assertThrows(
                IllegalArgumentException.class, () -> OrderPricingRules.validate(order("3.00")));
    }

    @Test
    void legacyDoesNotInventPromotionDetails() {
        var legacy = new TradeOrder(1L, "OLD", 1L, n("10.00"), "PAID", List.of(), null);
        assertDoesNotThrow(() -> OrderPricingRules.validate(legacy));
        assertNull(legacy.discountAmount());
    }
}
