package com.lemnt.mall.domain.order.model;

import com.lemnt.mall.domain.checkout.model.CheckoutItem;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 交易订单聚合根：订单金额和商品信息来自服务端快照。
 */
public record OrderSnapshot(
        Long id,
        String orderNo,
        Long memberId,
        BigDecimal totalAmount,
        String status,
        List<CheckoutItem> items,
        LocalDateTime createTime
) implements Serializable {
    /**
     * 创建交易订单实例。
     */
    public OrderSnapshot {
        items = items == null ? List.of() : List.copyOf(items);
    }

}
