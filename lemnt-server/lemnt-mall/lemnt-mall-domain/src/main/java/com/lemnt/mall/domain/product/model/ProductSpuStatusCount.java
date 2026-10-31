package com.lemnt.mall.domain.product.model;

import java.io.Serializable;

/** 商品状态数量。 */
public record ProductSpuStatusCount(
        long onShelf,
        long offShelf,
        long recycled,
        long soldOut,
        long lowStock
) implements Serializable {
}
