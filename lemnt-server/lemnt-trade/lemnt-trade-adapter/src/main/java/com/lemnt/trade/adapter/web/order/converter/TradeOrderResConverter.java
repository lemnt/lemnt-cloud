package com.lemnt.trade.adapter.web.order.converter;

import com.lemnt.trade.adapter.web.order.res.TradeOrderDetailRes;
import com.lemnt.trade.adapter.web.order.res.TradeOrderItemRes;
import com.lemnt.trade.application.order.dto.TradeOrderDetailDTO;

/** 订单详情响应转换。 */
public final class TradeOrderResConverter {

    private TradeOrderResConverter() {
    }

    /** 将应用查询结果转换为后台订单详情。 */
    public static TradeOrderDetailRes detail(TradeOrderDetailDTO order) {
        return new TradeOrderDetailRes(order.id(), order.orderNo(), order.memberId(), order.totalAmount(),
                order.status(), order.items().stream().map(item -> new TradeOrderItemRes(item.productId(),
                item.productCode(),
                item.productName(),
                item.unitPrice(),
                item.quantity(),
                item.lineAmount(),
                item.spuId(),
                item.spuName(),
                item.properties(),
                item.picUrl())).toList());
    }
}
