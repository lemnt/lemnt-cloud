package com.lemnt.mall.application.promotion;

import com.lemnt.mall.application.coupon.service.CouponIssuanceService;
import com.lemnt.mall.application.promotion.service.RewardGiftService;
import com.lemnt.mall.domain.settlement.model.SettlementOrder;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/** 营销赠券按订单快照补足数量，支付回调重试不重复发放。 */
class RewardGiftServiceTest {
    final CouponIssuanceService issuance = mock(CouponIssuanceService.class);
    final RewardGiftService service = new RewardGiftService(issuance);

    SettlementOrder order() {
        return new SettlementOrder(
                1L,
                "TR1",
                7L,
                BigDecimal.TEN,
                "PAID",
                List.of(),
                null,
                0L,
                0L,
                null,
                null,
                null,
                Map.of(3L, 2));
    }

    @Test
    void passesStableSequencesForEachCouponOnRetry() {
        service.issuePaid(order());
        service.issuePaid(order());
        verify(issuance, times(2)).gift(7L, 3L, "TR1", 1);
        verify(issuance, times(2)).gift(7L, 3L, "TR1", 2);
        verifyNoMoreInteractions(issuance);
    }
}
