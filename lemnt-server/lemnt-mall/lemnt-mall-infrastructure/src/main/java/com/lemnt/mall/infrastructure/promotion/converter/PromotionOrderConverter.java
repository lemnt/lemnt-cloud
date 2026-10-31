package com.lemnt.mall.infrastructure.promotion.converter;

import com.lemnt.mall.domain.promotion.model.PromotionOrder;
import com.lemnt.mall.infrastructure.promotion.dataobject.PromotionOrderDO;

/** 活动订单映射转换。 */
public final class PromotionOrderConverter {
    private PromotionOrderConverter() {}

    public static PromotionOrder model(PromotionOrderDO row) {
        if (row == null) { return null; }
        return new PromotionOrder(row.getId(), row.getOrderNo(), row.getRequestNo(), row.getMemberId(),
                row.getActivityType(), row.getActivityId(), row.getActivityRecordId(), row.getHeadId(),
                row.getSkuId(), row.getQuantity(), row.getActivityPrice(), row.getUsePoint(),
                row.getRefundedQuantity(), row.getStatus(), row.getFailureReason(), row.getCreateTime(),
                row.getUpdateTime());
    }

    public static PromotionOrderDO row(PromotionOrder value) {
        var row = new PromotionOrderDO();
        row.setId(value.id());
        row.setOrderNo(value.orderNo());
        row.setRequestNo(value.requestNo());
        row.setMemberId(value.memberId());
        row.setActivityType(value.activityType());
        row.setActivityId(value.activityId());
        row.setActivityRecordId(value.activityRecordId());
        row.setHeadId(value.headId());
        row.setSkuId(value.skuId());
        row.setQuantity(value.quantity());
        row.setActivityPrice(value.activityPrice());
        row.setUsePoint(value.usePoint());
        row.setRefundedQuantity(value.refundedQuantity());
        row.setStatus(value.status());
        row.setFailureReason(value.failureReason());
        row.setCreateTime(value.createTime());
        row.setUpdateTime(value.updateTime());
        return row;
    }
}
