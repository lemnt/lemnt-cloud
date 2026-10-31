package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.CombinationProductGateway;
import com.lemnt.mall.domain.promotion.model.CombinationProduct;
import com.lemnt.mall.infrastructure.promotion.converter.CombinationConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.CombinationProductDO;
import com.lemnt.mall.infrastructure.promotion.mapper.CombinationProductMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 拼团活动商品仅操作商品表。 */
@Component
@RequiredArgsConstructor
public class CombinationProductGatewayImpl implements CombinationProductGateway {
    private final CombinationProductMapper mapper;

    @Override
    public List<CombinationProduct> byActivityId(Long activityId) {
        return mapper.selectList(Wrappers.<CombinationProductDO>lambdaQuery()
                        .eq(CombinationProductDO::getActivityId, activityId)
                        .orderByAsc(CombinationProductDO::getId))
                .stream().map(CombinationConverter::model).toList();
    }

    @Override
    public List<CombinationProduct> byActivityIds(List<Long> activityIds) {
        if (activityIds == null || activityIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectList(Wrappers.<CombinationProductDO>lambdaQuery()
                        .in(CombinationProductDO::getActivityId, activityIds)
                        .orderByAsc(CombinationProductDO::getId))
                .stream().map(CombinationConverter::model).toList();
    }

    @Override
    public CombinationProduct byActivityAndSku(Long activityId, Long skuId) {
        return CombinationConverter.model(mapper.selectOne(Wrappers.<CombinationProductDO>lambdaQuery()
                .eq(CombinationProductDO::getActivityId, activityId)
                .eq(CombinationProductDO::getSkuId, skuId)));
    }

    @Override
    public void replace(Long activityId, List<CombinationProduct> products) {
        deleteByActivity(activityId);
        for (var item : products) {
            mapper.insert(CombinationConverter.row(new CombinationProduct(null, activityId, item.spuId(),
                    item.skuId(), item.combinationPrice(), item.activityStatus(), item.activityStartTime(),
                    item.activityEndTime())));
        }
    }

    @Override
    public void deleteByActivity(Long activityId) {
        mapper.delete(Wrappers.<CombinationProductDO>lambdaQuery()
                .eq(CombinationProductDO::getActivityId, activityId));
    }
}
