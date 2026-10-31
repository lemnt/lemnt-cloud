package com.lemnt.mall.infrastructure.promotion.gateway;

import com.lemnt.mall.domain.promotion.gateway.PromotionOrderItemGateway;
import com.lemnt.mall.domain.promotion.model.PromotionOrderItem;
import com.lemnt.mall.infrastructure.promotion.converter.PromotionOrderItemConverter;
import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionOrderItemDO;
import com.lemnt.mall.infrastructure.promotion.mapper.PromotionOrderItemMapper;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import java.util.List;

/** 活动订单商品行仅操作本表。 */
@Component
@RequiredArgsConstructor
public class PromotionOrderItemGatewayImpl implements PromotionOrderItemGateway {
    private final PromotionOrderItemMapper mapper;

    @Override
    public List<PromotionOrderItem> byOrderNo(String orderNo) {
        return mapper.selectList(Wrappers.<PromotionOrderItemDO>lambdaQuery()
                        .eq(PromotionOrderItemDO::getOrderNo, orderNo)
                        .orderByAsc(PromotionOrderItemDO::getSkuId))
                .stream().map(PromotionOrderItemConverter::model).toList();
    }

    @Override
    public void saveAll(String orderNo, List<PromotionOrderItem> items) {
        for (var item : items) {
            mapper.insert(PromotionOrderItemConverter.row(new PromotionOrderItem(null, orderNo,
                    item.skuId(), item.quantity(), item.usePoint(), 0)));
        }
    }

    @Override
    public boolean increaseRefunded(String orderNo, Long skuId, Integer expected, Integer quantity) {
        return mapper.update(null, Wrappers.<PromotionOrderItemDO>lambdaUpdate()
                .eq(PromotionOrderItemDO::getOrderNo, orderNo)
                .eq(PromotionOrderItemDO::getSkuId, skuId)
                .eq(PromotionOrderItemDO::getRefundedQuantity, expected)
                .set(PromotionOrderItemDO::getRefundedQuantity, expected + quantity)) == 1;
    }
}
