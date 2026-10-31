package com.lemnt.mall.application.promotion.service;

import com.lemnt.common.core.exception.ServiceException;
import com.lemnt.mall.application.promotion.dto.SeckillJoinDTO;
import com.lemnt.mall.domain.promotion.gateway.SeckillActivityGateway;
import com.lemnt.mall.domain.promotion.gateway.SeckillConfigGateway;
import com.lemnt.mall.domain.promotion.gateway.SeckillProductGateway;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/** 秒杀下单前校验及活动库存扣减/恢复，供订单链路调用。 */
@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class SeckillStockService {
    private final SeckillActivityGateway activities;
    private final SeckillConfigGateway configs;
    private final SeckillProductGateway products;

    private void require(boolean condition, String message) {
        if (!condition) {
            throw new ServiceException(message, 400);
        }
    }

    private LocalTime parse(String value) {
        return LocalTime.parse(value.length() == 5 ? value + ":00" : value);
    }

    /** 校验活动状态、活动时间、当前时段、限购和库存后返回秒杀价。 */
    @Transactional(readOnly = true)
    public SeckillJoinDTO validateJoin(Long activityId, Long skuId, Integer count) {
        require(count != null && count > 0, "购买数量不合法");
        var activity = activities.find(activityId);
        require(activity != null, "秒杀活动不存在");
        require(Objects.equals(activity.status(), 0), "秒杀活动已关闭");
        var now = LocalDateTime.now();
        require(!now.isBefore(activity.startTime()) && now.isBefore(activity.endTime()), "秒杀活动不在进行时间");
        var config = configs.byStatus(0).stream()
                .filter(item -> activity.configIds().contains(item.id())
                        && !now.toLocalTime().isBefore(parse(item.startTime()))
                        && now.toLocalTime().isBefore(parse(item.endTime())))
                .findFirst().orElse(null);
        require(config != null, "当前不在秒杀时段");
        require(count <= activity.singleLimitCount(), "超过单次限购数量");
        var product = products.byActivityAndSku(activityId, skuId);
        require(product != null, "秒杀商品不存在");
        require(count <= product.stock(), "秒杀库存不足");
        return new SeckillJoinDTO(activity.name(), activity.totalLimitCount(), product.seckillPrice());
    }

    /** 扣减活动商品和活动库存；两步在同一事务内按活动行锁串行。 */
    public void decreaseStock(Long activityId, Long skuId, Integer count) {
        require(count != null && count > 0, "扣减数量不合法");
        var activity = activities.lockById(activityId);
        require(activity != null, "秒杀活动不存在");
        var product = products.byActivityAndSku(activityId, skuId);
        require(product != null && count <= product.stock() && count <= activity.stock(), "秒杀库存不足");
        require(products.decreaseStock(activityId, skuId, count), "秒杀商品库存不足");
        require(activities.decreaseStock(activityId, count), "秒杀活动库存不足");
    }

    /** 恢复活动库存，活动或商品已删除时跳过，避免阻断订单取消。 */
    public void increaseStock(Long activityId, Long skuId, Integer count) {
        require(count != null && count > 0, "恢复数量不合法");
        if (!products.increaseStock(activityId, skuId, count)) {
            log.warn("秒杀商品不存在，跳过库存恢复, activityId={}, skuId={}", activityId, skuId);
            return;
        }
        if (!activities.increaseStock(activityId, count)) {
            log.warn("秒杀活动不存在，跳过活动库存恢复, activityId={}", activityId);
        }
    }
}
