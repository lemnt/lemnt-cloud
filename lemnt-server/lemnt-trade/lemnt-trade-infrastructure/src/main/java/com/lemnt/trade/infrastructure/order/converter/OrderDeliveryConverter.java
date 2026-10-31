package com.lemnt.trade.infrastructure.order.converter;

import com.lemnt.trade.domain.order.model.TradeOrderDelivery;
import com.lemnt.trade.infrastructure.order.dataobject.TradeOrderDO;

import java.math.BigDecimal;

/** 订单收件快照与独立数据库列转换。 */
public final class OrderDeliveryConverter {
    private OrderDeliveryConverter() {
    }

    /** 历史记录没有配送方式时保持为空。 */
    public static TradeOrderDelivery from(TradeOrderDO row) {
        return row.getDeliveryType() == null ? null : new TradeOrderDelivery(row.getAddressId(),
                row.getDeliveryType(), row.getReceiverName(), row.getReceiverMobile(), row.getReceiverAreaId(),
                row.getReceiverDetailAddress(), row.getDeliveryPrice());
    }

    /** 冻结配送信息，后续支付/发货不重读会员地址。 */
    public static void fill(TradeOrderDelivery value, TradeOrderDO row) {
        if (value == null
        ) {
            row.setDeliveryPrice(BigDecimal.ZERO.setScale(2));
            return;
        }
        row.setAddressId(value.addressId());
        row.setDeliveryType(value.deliveryType());
        row.setReceiverName(value.receiverName());
        row.setReceiverMobile(value.receiverMobile());
        row.setReceiverAreaId(value.receiverAreaId());
        row.setReceiverDetailAddress(value.receiverDetailAddress());
        row.setDeliveryPrice(value.deliveryPrice());
    }
}
