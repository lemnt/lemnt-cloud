package com.lemnt.order.infrastructure.order.converter;

import com.lemnt.order.domain.order.model.TradeOrderItem;
import com.lemnt.order.infrastructure.order.dataobject.TradeOrderItemDO;

import com.alibaba.fastjson2.JSON;

import java.util.List;

/** 订单项只读取落库快照，不回查当前商品。 */
public final class OrderItemConverter {
    private OrderItemConverter() {}

    /** JSON在持久化边界编码，未知快照不替换成空规格。 */
    public static TradeOrderItemDO toDO(Long orderId, TradeOrderItem value) {
        return new TradeOrderItemDO()
                .setOrderId(orderId)
                .setCommentStatus(false)
                .setProductId(value.productId())
                .setProductCode(value.productCode())
                .setProductName(value.productName())
                .setUnitPrice(value.unitPrice())
                .setQuantity(value.quantity())
                .setLineAmount(value.lineAmount())
                .setPayAmount(value.payAmount())
                .setDeliveryPrice(value.deliveryPrice())
                .setSpuId(value.spuId())
                .setSpuName(value.spuName())
                .setSpecs(value.specs() == null ? null : JSON.toJSONString(value.specs()))
                .setPicUrl(value.picUrl())
                .setDiscountAmount(value.discountAmount())
                .setDeliveryOriginalPrice(value.deliveryOriginalPrice())
                .setDeliveryDiscountPrice(value.deliveryDiscountPrice())
                .setAdjustmentAmount(value.adjustmentAmount())
                .setUsePoint(value.usePoint())
                .setGivePoint(value.givePoint())
                .setRefundedQuantity(value.refundedQuantity());
    }

    /** 从原订单项读取完整商品信息。 */
    public static TradeOrderItem toModel(TradeOrderItemDO row) {
        return new TradeOrderItem(
                row.getProductId(),
                row.getProductCode(),
                row.getProductName(),
                row.getUnitPrice(),
                row.getQuantity(),
                row.getLineAmount(),
                row.getPayAmount(),
                row.getDeliveryPrice(),
                row.getSpuId(),
                row.getSpuName(),
                specs(row.getSpecs()),
                row.getPicUrl(),
                row.getId(),
                row.getCommentStatus(),
                row.getDiscountAmount(),
                row.getDeliveryOriginalPrice(),
                row.getDeliveryDiscountPrice(),
                row.getAdjustmentAmount(),
                row.getUsePoint(),
                row.getGivePoint(),
                row.getRefundedQuantity());
    }

    /** 区分历史NULL和已知空规格，并兼容旧四字段和名值JSON。 */
    public static List<TradeOrderItem.Spec> specs(String json) {
        return SpecSnapshotJson.read(json, TradeOrderItem.Spec.class);
    }
}
