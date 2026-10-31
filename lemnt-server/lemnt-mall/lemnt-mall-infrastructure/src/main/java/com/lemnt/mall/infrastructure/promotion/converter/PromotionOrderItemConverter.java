package com.lemnt.mall.infrastructure.promotion.converter;

import com.lemnt.mall.domain.promotion.model.PromotionOrderItem;
import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionOrderItemDO;

/** 活动订单商品行转换。 */
public final class PromotionOrderItemConverter {
    private PromotionOrderItemConverter() {}

    public static PromotionOrderItem model(PromotionOrderItemDO row) {
        if (row == null) { return null; }
        return new PromotionOrderItem(row.getId(), row.getOrderNo(), row.getSkuId(), row.getQuantity(),
                row.getUsePoint(), row.getRefundedQuantity());
    }

    public static PromotionOrderItemDO row(PromotionOrderItem value) {
        var row = new PromotionOrderItemDO();
        row.setId(value.id());
        row.setOrderNo(value.orderNo());
        row.setSkuId(value.skuId());
        row.setQuantity(value.quantity());
        row.setUsePoint(value.usePoint());
        row.setRefundedQuantity(value.refundedQuantity());
        return row;
    }
}
