package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.DiscountProductGateway;
import com.lemnt.mall.domain.promotion.model.DiscountProduct;
import com.lemnt.mall.infrastructure.promotion.converter.PromotionConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.DiscountProductDO;
import com.lemnt.mall.infrastructure.promotion.mapper.DiscountProductMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 折扣商品仅操作活动商品表。 */
@Component
@RequiredArgsConstructor
public class DiscountProductGatewayImpl implements DiscountProductGateway {
    private final DiscountProductMapper mapper;

    @Override
    public List<DiscountProduct> byActivities(List<Long> activityIds) {
        if (activityIds.isEmpty()) {
            return List.of();
        }
        return mapper.selectList(Wrappers.<DiscountProductDO>lambdaQuery()
                .in(DiscountProductDO::getActivityId, activityIds))
                .stream().map(PromotionConverter::model).toList();
    }

    @Override
    public void replace(Long activityId, List<DiscountProduct> products) {
        deleteByActivity(activityId);
        for (var item : products) {
            mapper.insert(PromotionConverter.row(new DiscountProduct(null, activityId,
                    item.spuId(), item.skuId(), item.discountType(), item.discountPercent(), item.discountPrice())));
        }
    }

    @Override
    public void deleteByActivity(Long activityId) {
        mapper.delete(Wrappers.<DiscountProductDO>lambdaQuery().eq(DiscountProductDO::getActivityId, activityId));
    }
}
