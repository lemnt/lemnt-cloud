package com.lemnt.mall.application.promotion;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.promotion.service.AppPromotionHubService;
import com.lemnt.mall.domain.promotion.gateway.BargainActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.CombinationActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.DiscountActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.DiscountProductGateway;
import com.lemnt.mall.domain.promotion.gateway.PointActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.RewardActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.SeckillActivityGateway;
import com.lemnt.mall.domain.promotion.model.BargainActivity;
import com.lemnt.mall.domain.promotion.model.CombinationActivity;
import com.lemnt.mall.domain.promotion.model.DiscountActivity;
import com.lemnt.mall.domain.promotion.model.DiscountProduct;
import com.lemnt.mall.domain.promotion.model.PointActivity;
import com.lemnt.mall.domain.promotion.model.RewardActivity;
import com.lemnt.mall.domain.promotion.model.RewardRule;
import com.lemnt.mall.domain.promotion.model.SeckillActivity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 活动聚合只返回当前有效活动，并按分区和数量输出。 */
class AppPromotionHubServiceTest {
    final SeckillActivityGateway seckills = mock(SeckillActivityGateway.class);
    final CombinationActivityGateway combinations = mock(CombinationActivityGateway.class);
    final BargainActivityGateway bargains = mock(BargainActivityGateway.class);
    final PointActivityGateway points = mock(PointActivityGateway.class);
    final DiscountActivityGateway discounts = mock(DiscountActivityGateway.class);
    final RewardActivityGateway rewards = mock(RewardActivityGateway.class);
    final DiscountProductGateway discountProducts = mock(DiscountProductGateway.class);
    final AppPromotionHubService service = new AppPromotionHubService(seckills, combinations, bargains, points,
            discounts, rewards, discountProducts);
    final LocalDateTime now = LocalDateTime.now();

    SeckillActivity seckill(Long id, LocalDateTime start, LocalDateTime end) {
        return new SeckillActivity(id, 1L, "秒杀" + id, 0, null, start, end, 0, List.of(), 5, 2, 5, 5);
    }

    DiscountActivity discount(Long id, LocalDateTime start, LocalDateTime end) {
        return new DiscountActivity(id, "折扣" + id, 0, start, end, null);
    }

    RewardActivity reward(Long id) {
        return new RewardActivity(id, "满减" + id, 0, now.minusDays(1), now.plusDays(1), null, "PRICE",
                "ALL", List.of(), List.of(new RewardRule(BigDecimal.TEN, BigDecimal.ONE)));
    }

    @Test
    void sectionsFilterExpiredActivitiesAndRespectLimit() {
        when(seckills.enabled()).thenReturn(List.of(seckill(1L, now.minusDays(1), now.plusDays(1)),
                seckill(2L, now.minusDays(3), now.minusDays(2))));
        when(combinations.enabled()).thenReturn(List.of(new CombinationActivity(3L, "拼团", 1L, 5, 2,
                now.minusDays(1), now.plusDays(1), 2, true, 0, 24)));
        when(bargains.enabled()).thenReturn(List.of(new BargainActivity(4L, "砍价", now.minusDays(1),
                now.plusDays(1), 0, 1L, 1L, BigDecimal.TEN, BigDecimal.ONE, 5, 5, 2, 1, 1,
                BigDecimal.ONE, BigDecimal.ONE)));
        when(points.enabled()).thenReturn(List.of(new PointActivity(5L, 1L, 0, null, 0, 5, 5)));
        when(discounts.enabled()).thenReturn(List.of(discount(6L, now.minusDays(1), now.plusDays(1))));
        when(rewards.enabled()).thenReturn(List.of(reward(7L)));
        var sections = service.sections(10);
        assertEquals(List.of("SECKILL", "COMBINATION", "BARGAIN", "POINT", "DISCOUNT", "REWARD"),
                sections.stream().map(value -> value.type()).toList());
        assertEquals(1, sections.get(0).items().size());
        assertEquals(1, sections.get(3).items().size());
        assertEquals("积分商城", sections.get(3).items().get(0).name());
        assertEquals(1, service.sections(1).get(0).items().size());
        assertThrows(ServiceException.class, () -> service.sections(0));
    }

    @Test
    void ruleDetailsRequireActiveActivity() {
        when(discounts.find(6L)).thenReturn(discount(6L, now.minusDays(1), now.plusDays(1)));
        when(discountProducts.byActivities(List.of(6L))).thenReturn(List.of(new DiscountProduct(1L, 6L, 1L,
                2L, "PERCENT", 8000, null)));
        assertEquals(1, service.discount(6L).products().size());
        when(discounts.find(7L)).thenReturn(discount(7L, now.minusDays(3), now.minusDays(2)));
        assertThrows(ServiceException.class, () -> service.discount(7L));

        when(rewards.find(8L)).thenReturn(reward(8L));
        assertEquals("满减8", service.reward(8L).name());
        when(rewards.find(9L)).thenReturn(new RewardActivity(9L, "关闭", 1, now.minusDays(1), now.plusDays(1),
                null, "PRICE", "ALL", List.of(), List.of()));
        assertThrows(ServiceException.class, () -> service.reward(9L));
    }
}
