package com.lemnt.promotion.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.promotion.application.promotion.dto.AppDiscountActivityDTO;
import com.lemnt.promotion.application.promotion.dto.AppPromotionItemDTO;
import com.lemnt.promotion.application.promotion.dto.AppPromotionSectionDTO;
import com.lemnt.promotion.application.promotion.dto.AppRewardActivityDTO;
import com.lemnt.promotion.domain.promotion.gateway.BargainActivityGateway;
import com.lemnt.promotion.domain.promotion.gateway.CombinationActivityGateway;
import com.lemnt.promotion.domain.promotion.gateway.DiscountActivityGateway;
import com.lemnt.promotion.domain.promotion.gateway.DiscountProductGateway;
import com.lemnt.promotion.domain.promotion.gateway.PointActivityGateway;
import com.lemnt.promotion.domain.promotion.gateway.RewardActivityGateway;
import com.lemnt.promotion.domain.promotion.gateway.SeckillActivityGateway;
import com.lemnt.promotion.domain.promotion.model.BargainActivity;
import com.lemnt.promotion.domain.promotion.model.CombinationActivity;
import com.lemnt.promotion.domain.promotion.model.DiscountActivity;
import com.lemnt.promotion.domain.promotion.model.RewardActivity;
import com.lemnt.promotion.domain.promotion.model.SeckillActivity;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * 小程序活动聚合：只聚合已开启且当前在有效期内的活动，详情跳转各业务页面。
 */
@Component
@RequiredArgsConstructor
public class AppPromotionHubService {
    private static final int MAX_LIMIT = 50;

    private final SeckillActivityGateway seckills;
    private final CombinationActivityGateway combinations;
    private final BargainActivityGateway bargains;
    private final PointActivityGateway points;
    private final DiscountActivityGateway discounts;
    private final RewardActivityGateway rewards;
    private final DiscountProductGateway discountProducts;

    public List<AppPromotionSectionDTO> sections(Integer limit) {
        int size = limit == null ? 10 : limit;
        if (size < 1 || size > MAX_LIMIT) {
            throw new ServiceException("聚合数量须在1至50之间", 400);
        }
        List<AppPromotionSectionDTO> result = new ArrayList<>();
        result.add(
            new AppPromotionSectionDTO(
                "SECKILL",
                "限时秒杀",
                items(
                    seckills.enabled(),
                    size,
                    SeckillActivity::id,
                    SeckillActivity::name,
                    SeckillActivity::startTime,
                    SeckillActivity::endTime)));
        result.add(
            new AppPromotionSectionDTO(
                "COMBINATION",
                "拼团活动",
                items(
                    combinations.enabled(),
                    size,
                    CombinationActivity::id,
                    CombinationActivity::name,
                    CombinationActivity::startTime,
                    CombinationActivity::endTime)));
        result.add(
            new AppPromotionSectionDTO(
                "BARGAIN",
                "砍价活动",
                items(
                    bargains.enabled(),
                    size,
                    BargainActivity::id,
                    BargainActivity::name,
                    BargainActivity::startTime,
                    BargainActivity::endTime)));
        result.add(
            new AppPromotionSectionDTO(
                "POINT",
                "积分商城",
                points.enabled().stream()
                    .limit(size)
                    .map(
                        point ->
                            new AppPromotionItemDTO(
                                point.id(), "积分商城", null, null))
                    .toList()));
        result.add(
            new AppPromotionSectionDTO(
                "DISCOUNT",
                "限时折扣",
                items(
                    discounts.enabled(),
                    size,
                    DiscountActivity::id,
                    DiscountActivity::name,
                    DiscountActivity::startTime,
                    DiscountActivity::endTime)));
        result.add(
            new AppPromotionSectionDTO(
                "REWARD",
                "满减送",
                items(
                    rewards.enabled(),
                    size,
                    RewardActivity::id,
                    RewardActivity::name,
                    RewardActivity::startTime,
                    RewardActivity::endTime)));
        return result;
    }

    public AppDiscountActivityDTO discount(Long id) {
        var activity = discounts.find(id);
        if (activity == null
            || activity.status() != 0
            || !active(activity.startTime(), activity.endTime())) {
            throw new ServiceException("折扣活动不存在或已结束", 400);
        }
        return new AppDiscountActivityDTO(
            activity.id(),
            activity.name(),
            activity.startTime(),
            activity.endTime(),
            discountProducts.byActivities(List.of(activity.id())));
    }

    public AppRewardActivityDTO reward(Long id) {
        var activity = rewards.find(id);
        if (activity == null
            || activity.status() != 0
            || !active(activity.startTime(), activity.endTime())) {
            throw new ServiceException("满减活动不存在或已结束", 400);
        }
        return new AppRewardActivityDTO(
            activity.id(),
            activity.name(),
            activity.startTime(),
            activity.endTime(),
            activity.conditionType(),
            activity.productScope(),
            activity.rules());
    }

    private <T> List<AppPromotionItemDTO> items(
        List<T> values,
        int limit,
        Function<T, Long> id,
        Function<T, String> name,
        Function<T, LocalDateTime> start,
        Function<T, LocalDateTime> end) {
        return values.stream()
            .filter(value -> active(start.apply(value), end.apply(value)))
            .sorted(Comparator.comparing(start))
            .limit(limit)
            .map(
                value ->
                    new AppPromotionItemDTO(
                        id.apply(value),
                        name.apply(value),
                        start.apply(value),
                        end.apply(value)))
            .toList();
    }

    private boolean active(LocalDateTime start, LocalDateTime end) {
        var now = LocalDateTime.now();
        return start != null && end != null && !now.isBefore(start) && now.isBefore(end);
    }
}
