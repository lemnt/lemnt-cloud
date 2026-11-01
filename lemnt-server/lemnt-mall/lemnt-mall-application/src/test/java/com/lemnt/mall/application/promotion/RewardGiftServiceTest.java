package com.lemnt.mall.application.promotion;

import com.lemnt.mall.application.coupon.service.CouponIssuanceService;
import com.lemnt.mall.application.promotion.service.RewardGiftService;
import com.lemnt.mall.domain.coupon.gateway.CouponGateway;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 营销赠券按订单快照补足数量，支付回调重试不重复发放。 */
class RewardGiftServiceTest {
    final CouponGateway coupons = mock(CouponGateway.class);
    final CouponIssuanceService issuance = mock(CouponIssuanceService.class);
    final RewardGiftService service = new RewardGiftService(coupons, issuance);

    SettlementOrder order() {
        return new SettlementOrder(1L, "TR1", 7L, BigDecimal.TEN, "PAID", List.of(), null,
                0L, 0L, null, null, null, Map.of(3L, 2));
    }

    @Test
    void issuesMissingCouponsAndIsIdempotentOnRetry() {
        var issued = new AtomicLong();
        when(coupons.giftCount("TR1", 3L, 7L)).thenAnswer(invocation -> issued.get());
        doAnswer(invocation -> {
            issued.incrementAndGet();
            return null;
        }).when(issuance).gift(7L, 3L, "TR1");

        service.issuePaid(order());
        assertEquals(2L, issued.get());

        service.issuePaid(order());
        verify(issuance, times(2)).gift(7L, 3L, "TR1");
    }
}
