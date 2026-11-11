package com.lemnt.product.application.product.dto;

import java.io.Serializable;

/**
 * 商品状态统计结果。
 */
public record ProductSpuStatusCountDTO(
    long onShelf, long offShelf, long recycled, long soldOut, long lowStock)
    implements Serializable {
}
