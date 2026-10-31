package com.lemnt.trade.infrastructure.order.converter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.lemnt.trade.domain.order.model.TradeOrderItem;
import com.lemnt.trade.infrastructure.order.dataobject.TradeOrderItemDO;
import java.util.Map;
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
                .setProperties(value.properties() == null ? null : JSON.toJSONString(value.properties()))
                .setPicUrl(value.picUrl());
    }

    /** 从原订单项读取完整商品信息。 */
    public static TradeOrderItem toModel(TradeOrderItemDO row) {
        return new TradeOrderItem(row.getProductId(), row.getProductCode(), row.getProductName(), row.getUnitPrice(),
                row.getQuantity(), row.getLineAmount(), row.getPayAmount(), row.getDeliveryPrice(), row.getSpuId(),
                row.getSpuName(),
                properties(row.getProperties()), row.getPicUrl(), row.getId(), row.getCommentStatus());
    }

    /** 区分历史NULL和已知空规格。 */
    public static List<TradeOrderItem.Property> properties(String json) {
        if (json == null) {
            return null;
        }
        if (json.stripLeading().startsWith("{")) {
            var legacy = JSON.parseObject(json, new TypeReference<Map<String, String>>() {});
            return legacy.entrySet().stream().sorted(Map.Entry.comparingByKey())
                    .map(entry -> new TradeOrderItem.Property(null, entry.getKey(), null, entry.getValue())).toList();
        }
        return JSON.parseArray(json, TradeOrderItem.Property.class);
    }
}
