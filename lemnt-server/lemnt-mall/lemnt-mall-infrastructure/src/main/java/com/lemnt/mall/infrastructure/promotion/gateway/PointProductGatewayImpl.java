package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.PointProductGateway;
import com.lemnt.mall.domain.promotion.model.PointProduct;
import com.lemnt.mall.infrastructure.promotion.converter.PointConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.PointProductDO;
import com.lemnt.mall.infrastructure.promotion.mapper.PointProductMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 积分商城活动商品仅操作商品表。 */
@Component
@RequiredArgsConstructor
public class PointProductGatewayImpl implements PointProductGateway {
    private final PointProductMapper mapper;

    @Override
    public List<PointProduct> byActivityId(Long activityId) {
        return mapper.selectList(Wrappers.<PointProductDO>lambdaQuery()
                        .eq(PointProductDO::getActivityId, activityId).orderByAsc(PointProductDO::getId))
                .stream().map(PointConverter::model).toList();
    }

    @Override
    public List<PointProduct> byActivityIds(List<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectList(Wrappers.<PointProductDO>lambdaQuery()
                        .in(PointProductDO::getActivityId, activityIds).orderByAsc(PointProductDO::getId))
                .stream().map(PointConverter::model).toList();
    }

    @Override
    public PointProduct byActivityAndSku(Long activityId, Long skuId) {
        return PointConverter.model(mapper.selectOne(Wrappers.<PointProductDO>lambdaQuery()
                .eq(PointProductDO::getActivityId, activityId)
                .eq(PointProductDO::getSkuId, skuId)));
    }

    @Override
    public void replace(Long activityId, List<PointProduct> products) {
        deleteByActivity(activityId);
        for (var item : products) {
            mapper.insert(PointConverter.row(new PointProduct(null, activityId, item.spuId(), item.skuId(),
                    item.exchangeCount(), item.point(), item.price(), item.stock(), item.activityStatus())));
        }
    }

    @Override
    public void deleteByActivity(Long activityId) {
        mapper.delete(Wrappers.<PointProductDO>lambdaQuery()
                .eq(PointProductDO::getActivityId, activityId));
    }

    @Override
    public boolean decreaseStock(Long activityId, Long skuId, int count) {
        return mapper.update(null, Wrappers.<PointProductDO>lambdaUpdate()
                .eq(PointProductDO::getActivityId, activityId)
                .eq(PointProductDO::getSkuId, skuId)
                .ge(PointProductDO::getStock, count)
                .setDecrBy(PointProductDO::getStock, count)) == 1;
    }

    @Override
    public boolean increaseStock(Long activityId, Long skuId, int count) {
        return mapper.update(null, Wrappers.<PointProductDO>lambdaUpdate()
                .eq(PointProductDO::getActivityId, activityId)
                .eq(PointProductDO::getSkuId, skuId)
                .setIncrBy(PointProductDO::getStock, count)) == 1;
    }

    @Override
    public void updateStatus(Long activityId, Integer status) {
        mapper.update(null, Wrappers.<PointProductDO>lambdaUpdate()
                .eq(PointProductDO::getActivityId, activityId)
                .set(PointProductDO::getActivityStatus, status));
    }
}
