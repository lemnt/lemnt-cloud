package com.lemnt.mall.application.store.dto;

/**
 * 购物车增量加购数据传输对象。
 */
public record CartAdditionDTO(
        String requestNo,
        Long productId,
        int addedQuantity,
        int resultingQuantity
) implements java.io.Serializable {}
