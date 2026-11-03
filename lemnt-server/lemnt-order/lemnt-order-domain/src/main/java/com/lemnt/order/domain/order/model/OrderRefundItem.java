package com.lemnt.order.domain.order.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/** 可售后订单商品行快照。 */
public record OrderRefundItem(
        Long id,
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal lineAmount,
        BigDecimal payAmount,
        BigDecimal deliveryPrice,
        Long spuId,
        String spuName,
        List<Spec> specs,
        String picUrl)
        implements Serializable {
    /** 历史未保存商品元数据，保持为空，不查询现值补历史。 */
    public OrderRefundItem(
            Long id,
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal lineAmount,
            BigDecimal payAmount,
            BigDecimal deliveryPrice) {
        this(
                id,
                productId,
                productName,
                quantity,
                lineAmount,
                payAmount,
                deliveryPrice,
                null,
                null,
                null,
                null);
    }

    public OrderRefundItem {
        specs = specs == null ? null : List.copyOf(specs);
        deliveryPrice = deliveryPrice == null ? BigDecimal.ZERO.setScale(2) : deliveryPrice;
    }

    public OrderRefundItem(
            Long id, Long productId, String productName, Integer quantity, BigDecimal lineAmount) {
        this(id, productId, productName, quantity, lineAmount, null);
    }

    /** 历史快照缺少配送字段，读取时保留未配置状态。 */
    public OrderRefundItem(
            Long id,
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal lineAmount,
            BigDecimal payAmount) {
        this(
                id,
                productId,
                productName,
                quantity,
                lineAmount,
                payAmount,
                BigDecimal.ZERO.setScale(2));
    }

    /** 保存时的规格编号和名称，历史缺失编号保持为空。 */
    public record Spec(Long specId, String specName, Long specValueId, String specValueName)
            implements Serializable {}
}
