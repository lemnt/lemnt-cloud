package com.lemnt.trade.infrastructure.order.converter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.lemnt.trade.domain.order.model.TradeOrderItem;
import com.lemnt.trade.infrastructure.order.dataobject.TradeOrderItemDO;
import java.util.Map;
import java.util.TreeMap;

/** 订单项只读取落库快照，不回查当前商品。 */
public final class OrderItemConverter {
    private OrderItemConverter() {}

    /** JSON在持久化边界编码，未知快照不替换成空规格。 */
    public static TradeOrderItemDO toDO(Long orderId, TradeOrderItem value) {
        return new TradeOrderItemDO()
                .setOrderId(orderId)
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
                .setProperties(value.properties() == null ? null : JSON.toJSONString(new TreeMap<>(value.properties())))
                .setPicUrl(value.picUrl());
    }

    /** 从原订单项读取完整商品信息。 */
    public static TradeOrderItem toModel(TradeOrderItemDO row) {
        return new TradeOrderItem(row.getProductId(), row.getProductCode(), row.getProductName(), row.getUnitPrice(),
                row.getQuantity(), row.getLineAmount(), row.getPayAmount(), row.getDeliveryPrice(), row.getSpuId(),
                row.getSpuName(), properties(row.getProperties()), row.getPicUrl());
    }

    /** 区分历史NULL和已知空规格。 */
    public static Map<String, String> properties(String json) {
        return json == null ? null : JSON.parseObject(json, new TypeReference<Map<String, String>>() {});
    }
}
