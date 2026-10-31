package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.SeckillProductGateway;
import com.lemnt.mall.domain.promotion.model.SeckillProduct;
import com.lemnt.mall.infrastructure.promotion.converter.SeckillConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.SeckillProductDO;
import com.lemnt.mall.infrastructure.promotion.mapper.SeckillProductMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 秒杀活动商品仅操作活动商品表。 */
@Component
@RequiredArgsConstructor
public class SeckillProductGatewayImpl implements SeckillProductGateway {
    private final SeckillProductMapper mapper;

    @Override
    public List<SeckillProduct> byActivityId(Long activityId) {
        return SeckillConverter.products(mapper.selectList(Wrappers.<SeckillProductDO>lambdaQuery()
                .eq(SeckillProductDO::getActivityId, activityId).orderByAsc(SeckillProductDO::getId)));
    }

    @Override
    public List<SeckillProduct> byActivityIds(List<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return List.of();
        }
        return SeckillConverter.products(mapper.selectList(Wrappers.<SeckillProductDO>lambdaQuery()
                .in(SeckillProductDO::getActivityId, activityIds).orderByAsc(SeckillProductDO::getId)));
    }

    @Override
    public SeckillProduct byActivityAndSku(Long activityId, Long skuId) {
        return SeckillConverter.model(mapper.selectOne(Wrappers.<SeckillProductDO>lambdaQuery()
                .eq(SeckillProductDO::getActivityId, activityId).eq(SeckillProductDO::getSkuId, skuId)));
    }

    @Override
    public void replace(Long activityId, List<SeckillProduct> products) {
        deleteByActivity(activityId);
        for (var item : products) {
            mapper.insert(SeckillConverter.row(new SeckillProduct(null, activityId, item.configIds(),
                    item.spuId(), item.skuId(), item.seckillPrice(), item.stock(), item.activityStatus(),
                    item.activityStartTime(), item.activityEndTime())));
        }
    }

    @Override
    public void deleteByActivity(Long activityId) {
        mapper.delete(Wrappers.<SeckillProductDO>lambdaQuery()
                .eq(SeckillProductDO::getActivityId, activityId));
    }

    @Override
    public boolean decreaseStock(Long activityId, Long skuId, int count) {
        return mapper.update(null, Wrappers.<SeckillProductDO>lambdaUpdate()
                .eq(SeckillProductDO::getActivityId, activityId)
                .eq(SeckillProductDO::getSkuId, skuId)
                .ge(SeckillProductDO::getStock, count)
                .setDecrBy(SeckillProductDO::getStock, count)) == 1;
    }

    @Override
    public boolean increaseStock(Long activityId, Long skuId, int count) {
        return mapper.update(null, Wrappers.<SeckillProductDO>lambdaUpdate()
                .eq(SeckillProductDO::getActivityId, activityId)
                .eq(SeckillProductDO::getSkuId, skuId)
                .setIncrBy(SeckillProductDO::getStock, count)) == 1;
    }
}
